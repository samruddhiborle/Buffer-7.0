# Smart Workflow Task Scheduler

## Team Details

* Team No: 167
* Team Name: Stack and Queue
* Member 1: Samruddhi Borle
* Member 2: Ruchika Rangari
  

## Project Overview

A smart task scheduling system designed to efficiently manage and organize tasks using data structures.


## Problem Statement

Managing multiple tasks manually often leads to inefficiency, poor prioritization, and lack of structured workflow. This project provides a systematic approach to task handling.


## Domain

**Enterprise Systems & Process Optimization**
This project reflects real-world enterprise scenarios where workflows need to be structured, prioritized, and optimized. By using stack and queue, the system models task execution patterns such as last-in-first-out urgency handling and first-in-first-out scheduling, improving operational efficiency and decision-making.


## Data Structures & Algorithms Used

**1. HashMap**
Used to store tasks, employees, and login credentials, allowing fast access and updates in constant time **O(1)**.

**2. Graph (Task Dependencies)**
Tasks are modeled as a graph where each task is a node and dependencies are edges. This ensures proper linking and sequencing of tasks.

**3. Topological Sorting (Kahn’s Algorithm)**
Used to execute tasks in the correct order. It ensures that no task is executed before its dependencies and also helps detect cycles (invalid task dependencies).
A queue (LinkedList) is used to process tasks with zero dependencies.

**4. Priority Queue (Max Heap for Tasks)**
Used to select the most important task first based on:

* Priority (HIGH > MEDIUM > LOW)
* Deadline

**5. Priority Queue (Min Heap for Employees)**
Used to assign tasks to the least loaded employee, ensuring balanced workload distribution.

**6. Greedy Algorithm**
At each step, the system assigns the most suitable task to the most appropriate employee, resulting in an optimized scheduling approach.

## Key Features

* Task assignment and tracking
* Priority-based scheduling mechanism
* Efficient workflow management using data structures
* Structured and scalable task handling approach


## Demo Video

https://drive.google.com/drive/folders/154BaRBN49K64o6vPMji1G4KItZXp2E23?usp=sharing


## Conclusion

This project demonstrates how fundamental data structures can be leveraged to design efficient and scalable workflow systems used in enterprise environments.
