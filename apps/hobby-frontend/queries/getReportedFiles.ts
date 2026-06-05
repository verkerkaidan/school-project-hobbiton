import { gqlRequest } from "@/lib/graphQlClient";
import type { ReportedFile } from "@/types/reports";

const GET_REPORTED_FILES = `
  query GetReportedFiles {
    getReportedFiles {
      hash
      file
      contentType
      contentFlag
    }
  }
`;

export async function getReportedFiles(): Promise<ReportedFile[]> {
  const data = await gqlRequest<{ getReportedFiles: ReportedFile[] }>(GET_REPORTED_FILES);
  return data.getReportedFiles ?? [];
}


