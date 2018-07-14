#!/usr/bin/groovy

import com.vasya.BuildMetaInfo;
import com.vasya.SlackMessage;

/**
  * Notify CI results and tracking to team via Slack.
  * Slack Server is configured under Slack section of Jenkins > Configure System
  * @param status:  required
  * @param message: optional
  *
  * Format: $jobStartedByIcon $branchIcon $statusIcon @user $URL $nodeName $message
  */

def call(body) {
  echo "[cpl:notifySlack] params: ${body}"

  try {
    BuildMetaInfo bmi = new BuildMetaInfo()

    def jobTrigger = bmi.getJobTrigger()
    def lastCommit = bmi.getLastCommit()
    def testSummary = bmi.getTestSummary()

    jobTrigger.userId = jenkinsUserToSlackUser(jobTrigger.userId)
    lastCommit.userId = jenkinsUserToSlackUser(lastCommit.userId)

    def params = [ jobTrigger: jobTrigger, branchName: env.BRANCH_NAME, jobName: env.JOB_NAME, nodeName: env.NODE_NAME, currentBuild: currentBuild,
                   lastCommit: lastCommit, status: body.status, message: body.message,  buildUrl: env.BUILD_URL, testSummary: testSummary ]

    def slackMessage = new SlackMessage().getSlackNotification(params)

    echo "[cpl:notifySlack]: ${slackMessage}"
    if (body.status && body.status != 'UNKNOWN') {
      slackSend (color: slackMessage.barColor, message: slackMessage.text)
    }
  } catch (Exception x) {
    echo "[cpl:notifySlack] error in slackSend: ${x.getMessage()}"
  }
}

@NonCPS
def jenkinsUserToSlackUser(jenkinsUser) {
  return (hudson.model.User.getAll().find { it.getId() == jenkinsUser })?.getFullName() ?: ''
}
