import numpy as np
import matplotlib.pyplot as plt

# Parameters for the power-law distribution
exponent = 1.2
xmin = 1
xmax = 10000
num_points = 1000

# Generate x points in a linear scale
x = np.linspace(xmin, xmax, num_points)

# Calculate the power-law distribution with the given exponent
y = x**(exponent)


# Plot the power-law distribution
plt.plot(x, y)
plt.xlabel("x")
plt.ylabel("power-law distribution")
plt.title("Power-law distribution with exponent 1.2")
plt.grid(True)
plt.show()