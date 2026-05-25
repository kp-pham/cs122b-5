# cs122b-5
> Demonstration: [https://youtu.be/9yrFundq5_g](https://youtu.be/9yrFundq5_g)

The final project transforms the application from a monolithic architecture to a microservices architecture with Docker and Kubernetes.

## Features

* Microservices architecture
  * Containerized login and movies services with Docker images
  * Managed virtual machines with Kubernetes clusters to deploy containers on EC2 instances
  * Configured services and ingress rules to expose and load balance traffic to pods
  * Used Redis as centralized session store to save and share states between services