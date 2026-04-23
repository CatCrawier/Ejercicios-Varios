import numpy as np

A = np.array([[1, 2, 3],
              [2, 1, 2],
              [3, 1, 2]])

B = np.array([[2, 2, 2],
              [1, 1, 1],
              [3, 2, 1]])

print(A, B, sep="\n")
print(A + B)