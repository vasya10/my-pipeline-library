package com.vasya

/**
 * Env Variables Used:
 *  env.BRANCH_NAME
 *  env.BUILD_URL
 *  env.NODE_NAME
 *  env.JOB_NAME
 *
 * @params a map of several attributes, see SlackMessageTest.groovy for all variants
 * @return :whoStartedIcon: :whichBranchIcon: :whatStatusIcon: status - message [buildUrl - howLongRunning on whichDockerSlave] - testSummary
 */

def getSlackNotification(p) {

  def slackNotificationMap = [
    //who started this?
    GITHOOK:      [icon: 'git' ],
    TIMER:        [icon: 'alarm_clock' ],
    USER:         [icon: 'bust_in_silhouette' ],
    REPLAY:       [icon: 'repeat' ],
    //which stage?
    START:        [color: '#1E88E5', icon: 'launch' ],
    COMPILE:      [color: '#5E35B1', icon: 'gear' ],
    QA:           [color: '#FF8A65', icon: 'vertical_traffic_light' ],
    PUBLISH:      [color: '#FDD835', icon: 'package' ],
    FINISHED:     [color: '#607D8B', icon: 'end' ],
    //whats the build status?
    FAILURE:      [color: '#E53935', icon: 'negative_squared_cross_mark' ],
    SUCCESS:      [color: '#76FF03', icon: 'white_check_mark' ],
    CHANGED:      [color: '#009688', icon: 'arrows_counterclockwise' ],
    ABORTED:      [color: '#BDBDBD', icon: 'white_circle' ],
    UNKNOWN:      [color: '#FFFFFF', icon: 'grey_question' ],
    INFO:         [color: '#0000FF', icon: 'information_source' ]
  ]

  def message = p.message ? "${p.status} - ${p.message}" : "${p.status}"

  def jobTriggerIcon = slackNotificationMap[p.jobTrigger.startedBy].icon

  def committerSlackUserId = p.lastCommit.userId ? "@${p.lastCommit.userId}: " : " "
  def jobTriggerSlackUserId = p.jobTrigger.userId ? " @${p.jobTrigger.userId} " : " "

  def branchIcon = env.BRANCH_NAME == 'development' ? 'merge' : env.BRANCH_NAME == 'master' ? 'gem' : 'branch'

  def status = slackNotificationMap[p.status] ? p.status : 'UNKNOWN'
  def statusIcon = slackNotificationMap[status].icon
  def barColor = slackNotificationMap[status].color
  //For Success/Failure result, append the Test Summary
  def testSummary = ''
  if (status in ['SUCCESS', 'FAILURE', 'ABORTED']) {
    testSummary = p.testSummary ? " - ${p.testSummary}" : " - No test results found"
  }

  def text = ":${jobTriggerIcon}::${branchIcon}::${statusIcon}: `${message}`${jobTriggerSlackUserId}[${committerSlackUserId}*${p.lastCommit.message}*] - [${p.buildUrl} - ${p.currentBuild.durationString} on ${p.nodeName}]${testSummary}"

  echo "[cpl:getSlackNotification] ${text}"

  return [barColor: barColor, text: text]

}
