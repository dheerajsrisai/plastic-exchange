const label = {
  REQUESTED: "Awaiting review", VERIFIED: "Verified", SCHEDULED: "Finding agent", ASSIGNED: "Agent assigned",
  COLLECTED: "Collected", COMPLETED: "Completed", REJECTED: "Rejected",
};

export default function StatusBadge({ status }) {
  return <span className={`badge s-${status}`}>{label[status] || status}</span>;
}
