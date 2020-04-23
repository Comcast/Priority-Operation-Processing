Priority Operation Processing (POP)
======================================
What is Priority Operation Processing?
--------------------------------------

*   A workflow orchestration system where the workflow is scheduled as a unit giving resource priority once selected

*   Priority queuing and customizable scheduling algorithms

*   Customer aware for multi-tenant

*   Authentication via AWS Authorizer

*   Data object visibility for allowed customers

*   A JSON DAG based blueprint defines the execution flow

*   **Executes** operations within a workflow by spinning up on-demand Kubernetes Pods (dynamic resource allocation)

![images/pop-design.png](https://github.com/Comcast/Priority-Operation-Processing/wiki/images/pop-design.png)

Technologies
------------

*   API Gateway

*   AWS Authorizer

*   Lambda

*   SQS

*   Dynamo

*   CloudFormation

*   CloudWatch

*   Kubernetes

*   Kubernetes Annotations

*   Docker

*   Graphite



Components
==================

*Step 1: Submission*
-----------------
Workflow (Agenda)
POP's workflow data model is called an [Agenda](https://github.com/Comcast/Priority-Operation-Processing/wiki/AgendaAPI).

### Features

*   Simple list of operations

*   Node Graph / DAG model

*   Variable referencing between operations

*   Non-strict API for inputs / outputs of each operation

*   Agenda decoupling from underlying execution technologies / APIs. Each operation has a JSON payload that adheres to the API of the POP Handler that will perform the op.

*   The Agenda [Executor](Executor) will immediately run an operation as long as dependency variables are met

*   The Agenda Executor will look up Docker image by operation type and spin up a new Kubernetes Pod for the operation


*Step 2: Scheduling*
----------
[Scheduling](https://github.com/Comcast/Priority-Operation-Processing/wiki/Scheduling)

*Step 3: Execution*
-------------------------

[Execution](https://github.com/Comcast/Priority-Operation-Processing/wiki/Execution)

Technology Solutions
=======================

Scheduling
----------

*   Supports different scheduling algorithms for work queues (FIFO, Fairness, Priority)

*   Queues are scheduled asynchronously, no more heavy calculations at the time work is requested

*   Queues are defined by Insights (key words / identifiers / key-value pairs)

*   Queue sizes are configurable


Pluggable support for new technologies
--------------------------------------------------------------

*   Dynamic spinning up of Pods with Docker Images

*   No strict schema registration for new POP Handlers

*   No zoning hassles

*   No account permissions per handler

*   Simple Docker image configuration for new Handlers


Scalability / Cloud / Bare-metal
--------------------------------

*   Kubernetes

*   Each POP Handler does one job

*   Handlers are spun up dynamically and control their CPU requirements for dependent Pods


Multiple centers for processing content
---------------------------------------

*   Support for centers behind firewalls to have their own Kubernetes cluster do the content processing

*   Namespace segregation for customers wanting isolated resources within the same cluster


Parallel processing
-------------------

*   Able to parallel process operations with no waiting dependencies.


For more information visit the [wiki](https://github.com/Comcast/Priority-Operation-Processing/wiki).
