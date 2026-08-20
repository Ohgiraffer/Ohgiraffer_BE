/**
 * 고위험(TEAM_APPROVAL) 알럿의 다중 승인 현황을 추적한다.
 * 데모/개발 단계라 인메모리로 관리 (서버 재시작하면 초기화됨).
 * 나중에 운영 단계로 가면 Redis 등 영속 저장소로 옮기면 됨.
 */

const approvals = new Map(); // instanceId -> Set<approverId>
const rejections = new Map(); // instanceId -> Set<approverId>

function addApproval(instanceId, approverId) {
  if (!approvals.has(instanceId)) approvals.set(instanceId, new Set());
  approvals.get(instanceId).add(approverId);
  return approvals.get(instanceId).size;
}

function addRejection(instanceId, approverId) {
  if (!rejections.has(instanceId)) rejections.set(instanceId, new Set());
  rejections.get(instanceId).add(approverId);
  return rejections.get(instanceId).size;
}

function getApprovers(instanceId) {
  return Array.from(approvals.get(instanceId) || []);
}

function hasApproved(instanceId, approverId) {
  return approvals.get(instanceId)?.has(approverId) || false;
}

function hasRejected(instanceId, approverId) {
  return rejections.get(instanceId)?.has(approverId) || false;
}

function isFinalized(instanceId) {
  // 이미 실행되었거나 거부되어 더 이상 반응할 필요 없는 상태인지
  return approvals.get(instanceId)?.finalized === true;
}

function markFinalized(instanceId) {
  const set = approvals.get(instanceId) || new Set();
  set.finalized = true;
  approvals.set(instanceId, set);
}

module.exports = {
  addApproval,
  addRejection,
  getApprovers,
  hasApproved,
  hasRejected,
  isFinalized,
  markFinalized,
};
