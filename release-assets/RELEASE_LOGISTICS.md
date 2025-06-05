# Release Logistics

General development work is performed on feature branches, which are merged in to the `main` branch using pull requests (PRs). All feature branches should update `CHANGELOG.md` (see details below)  to ensure release documentation is accurate and complete.

## Preparing for a new release
When preparing for a new release, follow the steps below. All release branches should follow the pattern `release/major.minor.patch` and are **feature frozen** — only minor bug fixes are allowed.

1. Merge all PRs that should be part of the new release. Ensure that no critical features are left out.
2. Set temporary variables that specify the version number of the release and the next version to be used after the release (these variables are used in the shell commands in the next steps).
```bash
export RELEASE_VERSION=1.0.0  # <-- change this to the version number of the release
export NEXT_VERSION=1.0.1  # <-- change this to the next version number to be used after the release
```
3. Create and switch to a new release branch, following the naming convention `release/<version>`:
```bash
git checkout -b release/${RELEASE_VERSION}
git push --set-upstream origin release/${RELEASE_VERSION}
```
4. Review and update `CHANGELOG.md` in the root directory based on the merged PRs since the last release. Note that the changelogs are intended for humans, not machines, and the same types of changes should be grouped. Changes should be grouped by type (e.g., Added, Changed, Fixed), using the [Keep a Changelog](http://keepachangelog.com/) format. Example:
```markdown
# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and
this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [unreleased]
### Added
- Support for operator y.

## [1.0.0] - 2024-10-03

### Changed
- Refactored the query optimization algorithm.
```
5. Update the version number on the release branch:
```bash
mvn versions:set -DnewVersion=${RELEASE_VERSION} -DgenerateBackupPoms=false
git add pom.xml */pom.xml
git commit -m "bump up version to "${RELEASE_VERSION}
git push
```
6. Tag and push the release branch to GitHub:
```bash
git tag -a ${RELEASE_VERSION} -m "Release version "${RELEASE_VERSION}
git push origin ${RELEASE_VERSION}
```
7. In the GitHub UI, open a PR for the release branch, ensure that it is merged into `main`, and delete the release branch after merging.
8. Pull the latest changes from `main`, delete the release branch locally, and prepare the binaries:
```bash
git checkout main
git pull
git branch -d release/${RELEASE_VERSION}
mvn clean package && mvn package -pl hefquin-service -P build-war -am
```
9. Prepare the distribution folder:
```bash
# (optional) Preview the steps without copying or deleting files
release-assets/prepare-dist.sh --dry-run

# Build the distribution ZIP (e.g., HeFQUIN-1.0.0.zip)
release-assets/prepare-dist.sh
```
10. Push the new version to Maven Central by running the following command. Once this process succeeds, you may have to visit your account at https://central.sonatype.com/ to confirm by clicking 'Publish' for the "deployment". Yet, publishing can sometimes take several hours (but usually only takes a few minutes). So, before checking, perform the following step and we come back to checking in the step after that.
```bash
mvn deploy -Dgpg.skip=false
```
11. Create the release in GitHub. To this end, go to the `Releases` section and click [`Create a new release`](https://github.com/LiUSemWeb/HeFQUIN/releases/new). Use the version number as the release title (e.g., 1.0.0) and include the relevant parts of the changelog as the release notes. Attach the following binaries: `hefquin-service/target/hefquin-service-1.0.0.war` and `HeFQUIN-1.0.0.zip`.

12. Now go to the account at https://central.sonatype.com/ and check that the new version has been published there. Also, check at https://central.sonatype.com/search?q=hefquin

13. Increase the version number beyond the version of the release.
```bash
git checkout -b snapshot/${NEXT_VERSION}-SNAPSHOT
mvn versions:set -DnewVersion=${NEXT_VERSION}-SNAPSHOT -DgenerateBackupPoms=false
git add pom.xml */pom.xml
git commit -m "bump up version beyond ${RELEASE_VERSION}"
git push --set-upstream origin snapshot/${NEXT_VERSION}-SNAPSHOT
```
14. In the GitHub UI, open a PR for the snapshot-version branch, ensure that it is merged into `main`, and delete the snapshot-version branch after merging.
15. Pull the latest changes from `main` and delete the snapshot-version branch locally:
```bash
git checkout main
git pull
git branch -d snapshot/${NEXT_VERSION}-SNAPSHOT
```

##  Publish Docker Image
A Docker image (`latest`) is automatically pushed to the GitHub Container Registry (`ghcr.io`) when a new release is published as per step 11 above. You can check whether it is there by looking at https://github.com/LiUSemWeb/HeFQUIN/pkgs/container/hefquin. 

This Docker image can then be pulled easily by the following command:
```bash
docker pull ghcr.io/liusemweb/hefquin:latest
```

The GitHub Action that automatically pushes the image to the registry when a new release is made is specified in the workflow file [.github/workflows/docker.yml](https://github.com/LiUSemWeb/HeFQUIN/blob/main/.github/workflows/docker.yml).

## Publish to Maven Central

- Create an account at https://central.sonatype.com/
- Verify a namespace (e.g. io.github.liusemweb)
- Install GPG (GNU Privacy Guard) (see [details](https://gist.github.com/troyfontaine/18c9146295168ee9ca2b30c00bd1b41e?permalink_comment_id=3660126))
:
```bash
# Linux
sudo apt update
sudo apt install gnupg

# Mac
# install
brew install gnupg pinentry-mac
# make dir and change permissions
mkdir ~/.gnupg
chmod 700 ~/.gnupg
# restart gpg-agent
killall gpg-agent
# fixes issue with asking for passphrase securely
echo "pinentry-program $(brew --prefix)/bin/pinentry-mac" > ~/.gnupg/gpg-agent.conf
```

It may also be necessary to add `export GPG_TTY=$(tty)` to `~/.bash_profile` and source it:
```bash
source ~/.bash_profile
```

- Generate GPG key and publish it to a keyserver:
```bash
gpg --gen-key

gpg --list-keys
pub   rsa3072 2021-06-23 [SC] [expires: 2023-06-23]
      CA925CD6C9E8D064FF05B4728190C4130ABA0F98
uid           [ultimate] Central Repo Test <central@example.com>
sub   rsa3072 2021-06-23 [E] [expires: 2023-06-23]

gpg --keyserver hkp://keyserver.ubuntu.com --send-keys CA925CD6C9E8D064FF05B4728190C4130ABA0F98
```

- Generate a portal token for publishing. Go to https://central.sonatype.com/account and click the button to generate a user token. Add the server settings shown to your `~/.m2/settings.xml` file. If `settings.xml` doesn't exist, you need to create it as illustrated in the following snippet of XML. Notice that the `<id>` element in this snippet contains `central` whereas, in the user token shown by sonatype, it is `<id>${server}</id>`. It actually needs to be `central` (unless you have defined an environment variable called `server` with the value `central`).
```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>...</username>
      <password>...</password>
    </server>
  </servers>
</settings>
```

- You should now be ready to publish using the Maven Plugin. The root `pom.xml` will generate Javadoc, create sources attachments, handle GPG signing, create the bundles, and generate the checksum files on `mvn deploy`.