#!/usr/bin/env bash

# fail if any commands fails
set -e

assert_false() {
  local expression=$1
  local message=$2
  if [[ $(eval "$expression") ]]; then
    echo "Assertion failed: (>$expression) - $message"
    exit 1
  fi
}

assert_eq() {
  if [[ "$1" != "$2" ]]; then
    echo "$3"
    exit 1
  fi
}

assert_not_empty() {
  # check if the first argument is empty after trimming
  if [[ -z "${1//[[:space:]]/}" ]]; then
    echo "$2"
    exit 1
  fi
}

get_current_branch() { git rev-parse --abbrev-ref HEAD; }

assert_on_branch() {
  local branch=$1
  local current_branch
  current_branch=$(get_current_branch)
  if [[ "$current_branch" != "$branch" ]]; then
    echo "Not on branch $branch, aborting."
    exit 1
  fi
}

confirm() {
  local message=$1
  local response
  read -p "$message [n]: " response
  response=${response:-n}
  if [[ "$response" != "y" ]]; then
    echo "Aborting."
    exit 1
  fi
}

assert_clean_branch() { assert_false "git status --porcelain" "There are uncommitted changes, aborting."; }

increment_version() { echo "$1" | awk -F. '{$NF = $NF + 1;} 1' | sed 's/ /./g'; }

assert_tag_not_exists() { assert_false "git tag -l $1" "Tag $1 already exists, aborting."; }

assert_branch_not_exist() { assert_false "git branch -l $1" "Branch $1 already exists, aborting."; }

get_current_project_version() {
  grep "VERSION_NAME=" gradle.properties | cut -d'=' -f2
}

prompt_for_version() {
  local default_version=$1
  read -p "Enter the version [$default_version]: " actual_default_version
  actual_default_version=${actual_default_version:-$default_version}
  echo "$actual_default_version"
}

assert_branch_is_up_to_date() {
  git fetch
  HEAD=$(git rev-parse HEAD)
  UPSTREAM=$(git rev-parse '@{u}')
  assert_eq "$HEAD" "$UPSTREAM" "Local branch is not up-to-date, aborting."
}

update_version_in_properties_and_readme() {
  echo "Updating README.md and gradle.properties to [$1]"

  sed -i -e "s/VERSION_NAME=.*/VERSION_NAME=$1/g" gradle.properties
  # readme contains
  # id 'io.github.kayr.gradle.ezyquery' version '0.0.7'
  sed -i -e "s/id 'io\.github\.kayr\.gradle\.ezyquery' version '.*'/id 'io.github.kayr.gradle.ezyquery' version '$1'/g" README.md

}

MAIN_BRANCH="main"
CURRENT_BRANCH=$(get_current_branch)
RELEASE_VERSION=$(get_current_project_version)
RELEASE_VERSION_INCREMENTED=$(increment_version "$RELEASE_VERSION")

echo "check the current branch is clean"
assert_clean_branch

echo "check the current branch is up-to-date"
#NOOFF assert_branch_is_up_to_date

echo "check the current branch is $MAIN_BRANCH"
assert_eq "$CURRENT_BRANCH" "$MAIN_BRANCH" "Not on branch $MAIN_BRANCH, aborting."

NEW_VERSION=$(prompt_for_version "$RELEASE_VERSION_INCREMENTED")
assert_not_empty "$NEW_VERSION" "Version cannot be empty"

echo "check tag [$NEW_VERSION] and branch  [release/$NEW_VERSION] does not exist"
assert_tag_not_exists "$NEW_VERSION"
assert_branch_not_exist "release/$NEW_VERSION"

echo "  -> Creating branch release/$NEW_VERSION"
git checkout -b "release/$NEW_VERSION"

echo "  -> Updating README.md and gradle.properties to [$NEW_VERSION]"
update_version_in_properties_and_readme "$NEW_VERSION"

echo "  -> Committing changes"
git commit -am "Release $NEW_VERSION"

echo "  -> Pushing branch release/$NEW_VERSION"
#OFF git push --set-upstream origin "release/$NEW_VERSION"

echo "  -> Run Tests"
make test

# ======= PUBLISHING =====================
echo "  -> Publish groovy 3"
#make publish

echo "  -> Creating tag $NEW_VERSION"
git tag -a "$NEW_VERSION" -m "Release $NEW_VERSION"

echo "  -> Pushing tag $NEW_VERSION"
#OFF git push origin "$NEW_VERSION"

echo "  -> Switching to branch $MAIN_BRANCH"
git checkout "$MAIN_BRANCH"

echo "  -> Merging branch release/$NEW_VERSION into $MAIN_BRANCH"
git merge "release/$NEW_VERSION"

echo "  -> Pushing branch $MAIN_BRANCH"
#OFF git push
