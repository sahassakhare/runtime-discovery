/**
 * Loads a remote entry point script with SRI validation.
 *
 * @param remoteEntry - The URL of the script to load.
 * @param integrity - Optional SRI hash to validate the script content.
 * @returns A promise that resolves when the script loads successfully, or rejects on error.
 */
export async function loadRemoteWithSri(
  remoteEntry: string,
  integrity?: string,
  type: string = 'text/javascript'
): Promise<void> {
  console.log('[SRI Loader] Loading:', remoteEntry, 'Integrity:', integrity);
  return new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.src = remoteEntry;
    script.type = type;
    if (integrity) {
      script.integrity = integrity;
      script.crossOrigin = 'anonymous';
    }
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('Failed to load remoteEntry'));
    document.head.appendChild(script);
  });
}