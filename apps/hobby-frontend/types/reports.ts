import {ContentFlag} from "@/types/enums";

export type ReportReason =
  | "HARASSMENT"
  | "HATE_SPEECH"
  | "SPAM"
  | "INAPPROPRIATE_CONTENT"
  | "MISINFORMATION"
  | "IMPERSONATION"
  | "SCAM_FRAUD"
  | "VIOLENCE"
  | "SEXUAL_CONTENT"
  | "OTHER";

export type ReportStatus =
  | "PENDING"
  | "UNDER_REVIEW"
  | "RESOLVED"
  | "DISMISSED"
  | "ACTION_TAKEN";

export interface Report {
  id: string;
  /** UUID of the user who filed the report */
  initiator: string;
  /** UUID of the user being reported */
  target: string;
  reason: ReportReason;
  description: string;
  status: ReportStatus;
  createdAt?: string;
}

export interface ReportedFile {
  hash: string;
  file: string;
  contentFlag: ContentFlag;
  contentType: string;
}

export interface FileReport {
  hash: string;
  contentFlag: ContentFlag;
  note: string;
}

export const REASON_LABELS: Record<ReportReason, string> = {
  HARASSMENT: "Harassment",
  HATE_SPEECH: "Hate Speech",
  SPAM: "Spam",
  INAPPROPRIATE_CONTENT: "Inappropriate Content",
  MISINFORMATION: "Misinformation",
  IMPERSONATION: "Impersonation",
  SCAM_FRAUD: "Scam / Fraud",
  VIOLENCE: "Violence",
  SEXUAL_CONTENT: "Sexual Content",
  OTHER: "Other",
};

export const STATUS_CONFIG: Record<
  ReportStatus,
  { label: string; className: string }
> = {
  PENDING: {
    label: "Pending",
    className:
      "bg-amber-500/15 text-amber-400 border border-amber-500/25 ring-amber-500/10",
  },
  UNDER_REVIEW: {
    label: "Under Review",
    className:
      "bg-blue-500/15 text-blue-400 border border-blue-500/25 ring-blue-500/10",
  },
  RESOLVED: {
    label: "Resolved",
    className:
      "bg-emerald-500/15 text-emerald-400 border border-emerald-500/25 ring-emerald-500/10",
  },
  DISMISSED: {
    label: "Dismissed",
    className:
      "bg-stone-500/15 text-stone-400 border border-stone-500/25 ring-stone-500/10",
  },
  ACTION_TAKEN: {
    label: "Action Taken",
    className:
      "bg-rose-500/15 text-rose-400 border border-rose-500/25 ring-rose-500/10",
  },
};

export const ALL_STATUSES: ReportStatus[] = [
  "PENDING",
  "UNDER_REVIEW",
  "RESOLVED",
  "DISMISSED",
  "ACTION_TAKEN",
];
