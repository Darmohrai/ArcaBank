import { HttpResponse } from '@angular/common/http';
import { Observable, from, switchMap } from 'rxjs';

import { AccountService } from '../services/AccountService';
import {
  downloadBlob,
  readBlobErrorMessage,
  resolveDownloadFilename,
} from './download-file';

export function exportAccountStatementPdf(
  accountService: AccountService,
  accountId: string,
  currencyLabel: string
): Observable<void> {
  const fallbackName = `statement-${currencyLabel}-${new Date().toISOString().slice(0, 10)}.pdf`;

  return accountService.exportStatementPdf(accountId).pipe(
    switchMap((response) =>
      from(processStatementResponse(response, fallbackName))
    )
  );
}

async function processStatementResponse(
  response: HttpResponse<Blob>,
  fallbackName: string
): Promise<void> {
  const blob = response.body;
  if (!blob || blob.size === 0) {
    throw new Error('Сервер повернув порожній файл.');
  }

  const type = blob.type.toLowerCase();
  if (!type.includes('pdf')) {
    const message = await readBlobErrorMessage(blob);
    if (message) {
      throw new Error(message);
    }
  }

  const filename = resolveDownloadFilename(response, fallbackName);
  downloadBlob(blob, filename);
}

export async function statementExportErrorMessage(error: unknown): Promise<string> {
  const err = error as { error?: unknown };
  if (err?.error instanceof Blob) {
    const message = await readBlobErrorMessage(err.error);
    if (message) return message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return 'Не вдалося завантажити виписку. Спробуйте пізніше.';
}
