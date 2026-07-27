export function resizeImageToInstagram(file: File): Promise<File> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    const url = URL.createObjectURL(file);

    img.onload = () => {
      URL.revokeObjectURL(url);

      const targetWidth = 1080;
      const targetHeight = 1350; // 4:5
      const targetRatio = targetWidth / targetHeight;
      const imgRatio = img.width / img.height;

      let sx: number;
      let sy: number;
      let sWidth: number;
      let sHeight: number;

      if (imgRatio > targetRatio) {
        // 원본이 더 넓음 → 좌우 자름
        sHeight = img.height;
        sWidth = sHeight * targetRatio;
        sx = (img.width - sWidth) / 2;
        sy = 0;
      } else {
        // 원본이 더 좁거나 같음 → 위아래 자름
        sWidth = img.width;
        sHeight = sWidth / targetRatio;
        sx = 0;
        sy = (img.height - sHeight) / 2;
      }

      const canvas = document.createElement("canvas");
      canvas.width = targetWidth;
      canvas.height = targetHeight;
      const ctx = canvas.getContext("2d");
      if (!ctx) {
        reject(new Error("canvas context not available"));
        return;
      }

      ctx.drawImage(
        img,
        sx,
        sy,
        sWidth,
        sHeight,
        0,
        0,
        targetWidth,
        targetHeight,
      );

      canvas.toBlob(
        (blob) => {
          if (!blob) {
            reject(new Error("canvas to blob failed"));
            return;
          }
          const name = file.name.replace(/\.[^.]+$/, "") + ".jpg";
          resolve(new File([blob], name, { type: "image/jpeg" }));
        },
        "image/jpeg",
        0.92,
      );
    };

    img.onerror = () => reject(new Error("image load failed"));
    img.src = url;
  });
}
