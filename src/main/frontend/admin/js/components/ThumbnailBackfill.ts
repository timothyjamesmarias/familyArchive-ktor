/**
 * Thumbnail Backfill Component
 *
 * Handles thumbnail generation backfill and regeneration operations
 */

import { getCsrfHeaders } from '../../../js/shared/csrf';

interface ThumbnailBackfillResult {
  totalArtifacts: number;
  jobsEnqueued: number;
  skipped: number;
}

interface ThumbnailBackfillResponse {
  message: string;
  result: ThumbnailBackfillResult;
}

export function initializeThumbnailBackfill(): void {
  const backfillBtn = document.getElementById('backfillThumbnailsBtn');
  const regenerateBtn = document.getElementById('regenerateThumbnailsBtn');
  const sizeSelect = document.getElementById('thumbnailSize') as HTMLSelectElement;

  if (!backfillBtn || !regenerateBtn || !sizeSelect) {
    return;
  }

  // Backfill missing thumbnails
  backfillBtn.addEventListener('click', async () => {
    const size = parseInt(sizeSelect.value);
    await executeThumbnailOperation('backfill', size, backfillBtn as HTMLButtonElement);
  });

  // Regenerate all thumbnails
  regenerateBtn.addEventListener('click', async () => {
    const confirmed = confirm(
      'This will regenerate thumbnails for ALL image artifacts, even those that already have thumbnails. Continue?'
    );
    if (!confirmed) return;

    const size = parseInt(sizeSelect.value);
    await executeThumbnailOperation('regenerate', size, regenerateBtn as HTMLButtonElement);
  });
}

async function executeThumbnailOperation(
  operation: 'backfill' | 'regenerate',
  size: number,
  button: HTMLButtonElement
): Promise<void> {
  const endpoint = `/api/admin/thumbnails/${operation}`;
  const batchSize = 100;

  // Disable button
  const originalText = button.innerHTML;
  button.disabled = true;
  button.innerHTML = `
    <svg class="animate-spin w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
    </svg>
    <span>Processing...</span>
  `;

  try {
    const response = await fetch(`${endpoint}?size=${size}&batchSize=${batchSize}`, {
      method: 'POST',
      headers: getCsrfHeaders({
        'Content-Type': 'application/json',
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data: ThumbnailBackfillResponse = await response.json();

    // Show success message
    showThumbnailStatus(
      'success',
      data.message,
      `Enqueued ${data.result.jobsEnqueued} jobs. Skipped: ${data.result.skipped}. Monitor progress in JobRunr dashboard.`
    );

    // Reload page after 3 seconds to update stats
    setTimeout(() => {
      window.location.reload();
    }, 3000);
  } catch (error) {
    console.error('Thumbnail operation failed:', error);
    showThumbnailStatus(
      'error',
      'Operation failed',
      error instanceof Error ? error.message : 'An unknown error occurred'
    );

    // Re-enable button
    button.disabled = false;
    button.innerHTML = originalText;
  }
}

function showThumbnailStatus(
  type: 'success' | 'error' | 'info',
  message: string,
  details: string
): void {
  const statusContainer = document.getElementById('thumbnailStatus');
  const statusMessage = document.getElementById('thumbnailStatusMessage');
  const statusIcon = document.getElementById('thumbnailStatusIcon');
  const statusText = document.getElementById('thumbnailStatusText');
  const statusDetails = document.getElementById('thumbnailStatusDetails');

  if (!statusContainer || !statusMessage || !statusIcon || !statusText || !statusDetails) {
    return;
  }

  // Show container
  statusContainer.classList.remove('hidden');

  // Update styling based on type
  statusMessage.className = 'rounded-lg p-4 ';
  if (type === 'success') {
    statusMessage.classList.add(
      'bg-green-50',
      'dark:bg-green-900/20',
      'text-green-800',
      'dark:text-green-300'
    );
    statusIcon.classList.add('text-green-600', 'dark:text-green-400');
    statusIcon.innerHTML = `
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
    `;
  } else if (type === 'error') {
    statusMessage.classList.add(
      'bg-red-50',
      'dark:bg-red-900/20',
      'text-red-800',
      'dark:text-red-300'
    );
    statusIcon.classList.add('text-red-600', 'dark:text-red-400');
    statusIcon.innerHTML = `
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
    `;
  } else {
    statusMessage.classList.add(
      'bg-blue-50',
      'dark:bg-blue-900/20',
      'text-blue-800',
      'dark:text-blue-300'
    );
    statusIcon.classList.add('text-blue-600', 'dark:text-blue-400');
    statusIcon.innerHTML = `
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
    `;
  }

  statusText.textContent = message;
  statusDetails.textContent = details;
}
