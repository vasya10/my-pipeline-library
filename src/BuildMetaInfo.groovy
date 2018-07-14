/**
 * Provides various functions to collect build meta info.
 * Usually inspects the currentBuild.rawBuild property
 */

package com.vasya

import hudson.tasks.test.AbstractTestResultAction

/**
 * Finds the build cause
 * @return buildCause
 */
@NonCPS
def getBuildCause() {
  def buildCause
  try {
    def buildCauses = currentBuild.rawBuild.getCauses()
    for ( bc in buildCauses ) {
      if (bc != null) {
        // echo "[cpl:BuildMetaInfo] job trigger cause: ${bc.properties}"
        buildCause = bc
      }
    }
  } catch(x) {
    echo "[cpl:BuildMetaInfo] error in displayBuildCauses: ${x}"
  }

  return buildCause ? buildCause.properties : [:]
}

@NonCPS
def isJobStartedByTimer() {
  def jobStartedByTimer = false
  try {
    def buildCauses = currentBuild.rawBuild.getCauses()
    for ( bc in buildCauses ) {
      if (bc != null) {
        if (bc.getShortDescription().contains("Started by timer")) {
          jobStartedByTimer = true
        }
      }
    }
  } catch(x) {
    echo "[cpl:BuildMetaInfo] error in isJobStartedByTimer: ${x}"
  }

  return jobStartedByTimer
}

/**
 * Finds who started the build
 * @return [startedBy, userId]
 */
def getJobTrigger() {
  def bc = getBuildCause()

  def startedBy = ''
  def userId = ''

  if (!bc) return [startedBy: startedBy, userId: userId]

  if (bc.shortDescription == 'Branch event') {
    startedBy = 'GITHOOK'
  } else if (bc.shortDescription == 'Started by timer') {
    startedBy = 'TIMER'
  } else if (bc.shortDescription.indexOf("Replayed") >= 0) {
    startedBy = 'REPLAY'
  } else if (bc.userId) {
    userId = "${buildCause.userId}"
    startedBy = 'USER'
  }

  def jobTrigger = [startedBy: startedBy, userId: userId]
  // echo "[cpl:getJobTrigger]: ${jobTrigger}"

  return jobTrigger
}

/**
 * Finds the last commit from git for this build
 * @return [userId, commitMessage]
 */
def getLastCommit() {
  def commit = sh(returnStdout: true, script: 'git rev-parse HEAD')
  userId = sh(returnStdout: true, script: "git --no-pager show -s --format='%ae' ${commit}").trim().replace('@virtualinstruments.com', '')
  message = sh(returnStdout: true, script: 'git log -1 --pretty=%B').trim()

  def lastCommit = [userId: userId, message: message]
  echo "[cpl:getLastCommit]: ${lastCommit}"

  return lastCommit
}

/**
 * Retrieves the Test Summary for the build
 * @return [userId, commitMessage]
 */
def getTestSummary() {
  def testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
  def summary = ""

  if (testResultAction != null) {
    total = testResultAction.getTotalCount()
    failed = testResultAction.getFailCount()
    skipped = testResultAction.getSkipCount()

    summary = "Passed: " + (total - failed - skipped)
    summary = summary + (", Failed: " + failed)
    summary = summary + (", Skipped: " + skipped)
  }
  echo "[cpl:getTestSummary]: ${summary}"
  return summary
}
