import numpy as np

path = r"D:\NhanDangThiThayThiHo\FaceProctoring\python_backend\labels_arc.npy"
labels = np.load(path, allow_pickle=True)
print("Unique labels in file:", np.unique(labels))
input("\nNhấn Enter để thoát...")
