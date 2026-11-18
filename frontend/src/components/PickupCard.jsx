import StatusBadge from "./StatusBadge";

export default function PickupCard({ p, children }) {
  return (
    <article className="pickup">
      {p.imageUrl && <img src={p.imageUrl} alt="Plastic to recycle" />}
      <div className="pickup-body">
        <div className="row between">
          <strong>{p.plasticType} plastic</strong>
          <StatusBadge status={p.status} />
        </div>
        <p className="muted">
          {p.weightKg > 0 ? `${p.weightKg} kg collected` : `About ${p.estimatedWeightKg} kg`}
          {p.scheduledAt && ` · ${new Date(p.scheduledAt).toLocaleString()}`}
        </p>
        <p className="muted">{p.address}{p.area ? `, ${p.area}` : ""}</p>
        {p.aiType && (
          <p className="muted">
            AI check: {p.aiType} ({Math.round(p.aiConfidence * 100)}% sure)
            {p.needsReview && " – sent for manual review"}
          </p>
        )}
        {p.agentName && <p className="muted">Agent: {p.agentName}</p>}
        {p.pointsAwarded > 0 && <p className="points-line">+{p.pointsAwarded} points</p>}
        {children}
      </div>
    </article>
  );
}
