import { HttpResponse } from '@angular/common/http';

export function downloadBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  anchor.style.display = 'none';
  document.body.appendChild(anchor);
  anchor.click();
  document.body.removeChild(anchor);
  URL.revokeObjectURL(url);
}

export function filenameFromContentDisposition(
  header: string | null
): string | null {
  if (!header) return null;
  const utf8 = /filename\*=UTF-8''([^;]+)/i.exec(header);
  if (utf8?.[1]) {
    try {
      return decodeURIComponent(utf8[1].trim());
    } catch {
      return utf8[1].trim();
    }
  }
  const plain = /filename="?([^";]+)"?/i.exec(header);
  return plain?.[1]?.trim() ?? null;
}

export function resolveDownloadFilename(
  response: HttpResponse<Blob>,
  fallback: string
): string {
  const fromHeader = filenameFromContentDisposition(
    response.headers.get('Content-Disposition')
  );
  if (fromHeader) return fromHeader;
  const type = response.body?.type ?? '';
  if (type.includes('pdf')) return fallback.endsWith('.pdf') ? fallback : `${fallback}.pdf`;
  return fallback;
}

export async function readBlobErrorMessage(blob: Blob): Promise<string | null> {
  const type = blob.type.toLowerCase();
  if (!type.includes('json') && !type.includes('text')) {
    return null;
  }
  try {
    const text = await blob.text();
    if (!text.trim()) return null;
    try {
      const body = JSON.parse(text) as { message?: string };
      return body.message ?? text;
    } catch {
      return text;
    }
  } catch {
    return null;
  }
}
