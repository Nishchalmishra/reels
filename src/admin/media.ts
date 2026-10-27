export const MAX_VIDEO_BYTES = 50 * 1024 * 1024;
export const hash = async (blob: Blob) =>
  Array.from(
    new Uint8Array(
      await crypto.subtle.digest("SHA-256", await blob.arrayBuffer()),
    ),
  )
    .map((v) => v.toString(16).padStart(2, "0"))
    .join("");
export async function inspectVideo(
  file: File,
): Promise<{ duration: number; poster: Blob; sha: string; posterSha: string }> {
  if (file.size > MAX_VIDEO_BYTES || file.size === 0)
    throw Error("Choose a non-empty MP4 up to 50 MB.");
  if (!file.name.toLowerCase().endsWith(".mp4"))
    throw Error("Only MP4 videos are supported.");
  const url = URL.createObjectURL(file),
    v = document.createElement("video");
  v.muted = true;
  v.playsInline = true;
  v.preload = "auto";
  try {
    const result = await new Promise<{ duration: number; poster: Blob }>(
      (resolve, reject) => {
        const timer = setTimeout(
          () =>
            reject(
              Error(
                "Could not read this video. Check its format and try again.",
              ),
            ),
          45000,
        );
        const fail = (e: Error) => {
          clearTimeout(timer);
          reject(e);
        };
        v.onerror = () =>
          fail(
            Error(
              "This MP4 cannot be played by the browser. Re-encode it as H.264 MP4.",
            ),
          );
        v.onloadedmetadata = () => {
          if (
            !Number.isFinite(v.duration) ||
            v.duration < 1 ||
            v.duration > 1800 ||
            !v.videoWidth
          ) {
            fail(Error("Video must be between 1 second and 30 minutes."));
            return;
          }
          v.currentTime = Math.min(1, v.duration / 3);
        };
        v.onseeked = () => {
          const canvas = document.createElement("canvas");
          canvas.width = Math.min(480, v.videoWidth);
          canvas.height = Math.round(
            (canvas.width * v.videoHeight) / v.videoWidth,
          );
          const ctx = canvas.getContext("2d");
          if (!ctx) {
            fail(Error("Poster generation unavailable."));
            return;
          }
          ctx.drawImage(v, 0, 0, canvas.width, canvas.height);
          canvas.toBlob(
            (poster) => {
              clearTimeout(timer);
              poster
                ? resolve({ duration: v.duration, poster })
                : reject(Error("Could not create a poster."));
            },
            "image/jpeg",
            0.84,
          );
        };
        v.src = url;
      },
    );
    return {
      ...result,
      sha: await hash(file),
      posterSha: await hash(result.poster),
    };
  } finally {
    v.removeAttribute("src");
    v.load();
    URL.revokeObjectURL(url);
  }
}
