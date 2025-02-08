#!/bin/bash -e

modules=$(grep "^include(" settings.gradle.kts \
    | grep -E "(:testing|-testing|:smoke-tests)" \
    | sed 's/^include(\"//' \
    | sed 's/")$//')

echo "version: 3"
echo
echo "targets:"
echo "  only:"
echo "    # these modules are not published and so consumers will not be exposed to them"

for module in $modules; do
  echo "    - type: gradle"
  echo "      path: ./"
  echo "      target: '$module'"
done

echo
echo "experimental:"
echo "  gradle:"
echo "    configurations-only:"
echo "      # consumer will only be exposed to these dependencies"
echo "      - runtimeClasspath"
