const approvals = new Map();
const rejections = new Map();

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
