/**
 * Loads a remote entry point script with SRI validation.
 *
 * @param remoteEntry - The URL of the script to load.
 * @param integrity - Optional SRI hash to validate the script content.
 * @returns A promise that resolves when the script loads successfully, or rejects on error.
 */
export async function loadRemoteWithSri(
  remoteEntry: string,
  integrity?: string
): Promise<void> {
  return new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.src = remoteEntry;
    if (integrity) {
      script.integrity = integrity;
      script.crossOrigin = 'anonymous';
    }
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('Failed to load remoteEntry'));
    document.head.appendChild(script);
  });
}