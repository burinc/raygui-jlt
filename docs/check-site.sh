#!/usr/bin/env bash
# Assertions this project's documentation build must satisfy.
#
# Run by the shared site workflow in jlt-commons/ci-builds against the freshly
# built _site, with BASE_PATH exported. Lifted verbatim out of this repo's own
# site.yml when the build scaffolding moved to the shared workflow, so every
# check here predates that move and still means what it did.
#
# Run it locally the same way:
#   bb site:build && BASE_PATH=/raygui-jlt bash docs/check-site.sh

set -euo pipefail
out=_site

test -f "$out/index.html"       || { echo "no homepage generated"; exit 1; }
test -f "$out/guide/index.html" || { echo "no guide page generated"; exit 1; }
test -f "$out/css/screen.css"   || { echo "static assets missing"; exit 1; }

# The gallery is the point of this site. A missing asset dir is a
# warning inside the engine, deliberately, so it has to be an error
# here or the docs publish with every image broken.
shots=$(find "$out/demos" -type f 2>/dev/null | wc -l | tr -d ' ')
source_shots=$(find docs/demos -type f | wc -l | tr -d ' ')
test "$shots" = "$source_shots" \
  || { echo "copied $shots demo images, expected $source_shots"; exit 1; }

! grep -rq '{{site-base}}' "$out"/index.html "$out"/guide/*.html \
  || { echo "unrendered template variable"; exit 1; }

# The only diagram on this site is on the homepage, which is a
# hand-written template rather than a markdown fence. Engine v0.2.0
# detects that from the template's own source. Both directions fail
# quietly: a diagram page that drops the bundle renders unstyled
# source text, and a page without one that loads it costs every
# reader 3.4 MB for nothing.
grep -q 'pre class="mermaid"' "$out/index.html" \
  || { echo "the homepage diagram is missing"; exit 1; }
grep -q 'mermaid.min.js' "$out/index.html" \
  || { echo "the homepage has a diagram but is not loading mermaid"; exit 1; }
! grep -q 'mermaid.min.js' "$out/guide/index.html" \
  || { echo "a page with no diagram is loading mermaid"; exit 1; }

# The failure mode this site's base path exists to prevent. Served at
# jlt-commons.github.io/raygui-jlt/, a root-absolute URL loads the
# ORGANIZATION site's asset instead of this project's. The page still
# renders, wearing the wrong clothes, so nothing but a check catches it.
if grep -ohE '(href|src)="/[^"]*"' "$out"/index.html "$out"/404.html "$out"/guide/*.html \
     | grep -vE "=\"$BASE_PATH/"; then
  echo "the URLs above escape $BASE_PATH and would resolve against the org site"
  exit 1
fi

echo "build looks correct: $shots demo images, every URL under $BASE_PATH"
