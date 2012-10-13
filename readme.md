# Multimode Networks
Author: Jaroslav Kuchar

<a href="https://gephi.org/plugins/">Multimode Networks</a> plugin for <a href="http://www.gephi.org">Gephi</a>. 
This plugin allows multimode networks projection. You can project your bipartite (2-mode) graph to monopartite (one-mode) graph. The projection/transformation is based on the matrix multiplication approach and allows different types of transformations. The limitation is matrix multiplication - large matrix multiplication takes time and memory.

This version contains:

  * Projection of network
  * Graph coloring for bipartite network

# Tutorial

Transformation is available in menu: Window -> MultiMode Projections

1.  Load attributes
2.  Select attribute which represents type of node (eg. Person and Company)
3.  Select Transformation represented by matrix (vertical and horizontal) dimension combination (eg. Person-Company x Company-Person -> Person-Person network)
4.  You can decide to remove nodes, edges
5.  Run!

For bipartite network you can color your graph and generate "color" attribute in order to use it in this plugin for projection.
At the same time tou can check if your graph is bipartite. 