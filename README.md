# os-filesystem-simulator
Simulador virtual de sistema de archivos concurrente en Java puro. Implementa asignación encadenada, planificadores de disco, journaling y estructuras de datos personalizadas desde cero.

Proyecto académico de Sistemas Operativos (2526-2). Este simulador implementa un sistema de archivos concurrente gestionado por hilos y semáforos. Su núcleo está construido bajo una estricta restricción de no utilizar el framework de colecciones de Java (java.util.*), requiriendo el desarrollo de estructuras de datos propias (Nodos, Listas Enlazadas, Colas Concurrentes). Incluye interfaz gráfica (GUI) en Swing, algoritmos de planificación de disco (FIFO, SSTF, SCAN, C-SCAN), gestión de permisos (Admin/User) y recuperación ante fallos (Journaling) mediante serialización JSON.
