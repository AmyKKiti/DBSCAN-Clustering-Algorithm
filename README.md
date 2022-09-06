# DBSCAN-Clustering-Algorithm
Data clustering algorithm named DBSCAN - Density-Based Spatial Clustering of Applications with Noise. 
Given a large set of data points in a space of arbitrary dimension and given a distance metric, this algorithm can discover clusters of different shapes and sizes, marking as outliers isolated points in low-density regions. (i.e. points whose nearest
neighbors are too far away).

## Context
DBSCAN algorithm can be used to manage, for example, a taxi fleet in NYC by identifying the best waiting areas for your
vehicles. 
For this purpose, a large dataset of taxi trip records from 2009 will be used. 
Each record of this dataset includes the GPS coordinates of the starting point and end point for the corresponding trip contained in a simple comma-separated value file (csv).
By applying the DBSCAN algorithm, we can cluster the starting point locations of the trip records in the NYC 2009 taxi dataset. The centers of the largest clusters will become the waiting area for the taxi fleet. 
- Dataset used: All the trip records for January 15, 2009 between 12pm and 1pm
- minPts: The minimum number of points (a threshold) in the neighborhood of a point for this one
to be considered to belong to a dense region.
- Eps (ε): A distance measure that is used to identify the points in the neighborhood of any point.  

## Programming
The DBSCAN algorithm has been implemented in parts, in 4 different programming languages with different paradigms.

#### Java version - Object Oriented Programming
- The DBSCAN algorithm is implemented and produces 21,232 clusters according to the dataset of trip records for January 15.
- A UML diagram has been provided to clearly show the concepts used in OOP.

#### Golang version - Concurrent Programming
- The DBSCAN algorithm is concurrently run on partitions of the Trip Record data. To create these partitions, the geographical area is divided into a grid of
NxN. N threads are created to run the dbscan concurrently.
- This algorithm is based on the MapReduce pattern, widely used in concurrent programming.
- The producer-consumer pattern is used.
- A report of different running time resulting from using different threads has been documented.

#### Prolog version - Logical Programming
- This version focuses on the merging step of the MapReduce pattern from the previous version. Intersecting clusters from adjacent partitions are merged.
- All the predicates used have been documented with examples.

#### Scheme version - Functional Programming
- This version also implements the merging step of the MapReduce pattern, similar to the prolog version, but programmed in a functional paradigm.
