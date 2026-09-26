# [DD_7] Choose animation curves

## Context & Metadata

- **Author:** Emmanuel Pastor [Library Owner]
- **Status:** Draft
- **Target Date:** 2026-10-26
- **Stakeholders:**

  | Role          | Name            |
      |---------------|-----------------|
  | Feature Owner | Emmanuel Pastor |
  | Design Owner  | Emmanuel Pastor |
  | Implementers  | Emmanuel Pastor |
  | Reviewers     | Emmanuel Pastor |
  | UX            | Emmanuel Pastor |
  | QA            | Emmanuel Pastor |
  | Security      | Emmanuel Pastor |

## Problem & Scope

### Current State

There are two phases of progress with fixed animation curves.

The first phase uses a linear curve, while the second phase uses an asymptotic curve.

A final finishing phase is used to animate the progress to 100% once the task has been declared as completed by the
user.

Animations cannot be configured.

The only configurable parameter is the ETA of the task for which the progress is faked.

### Problem Statement

The user cannot configure any animation curve. Affected users are: library users, developers, who want control over the
progress animation. The value of this feature is to make the library more flexible and customizable to be useful to more
users.

### Goals

- Provide the ability to define and use custom animation curves for the first and second phases.
- Allow users to choose from a set of predefined animation curves.

### Non-Goals & Out of Scope

- Change the number of phases in the progress animation.
- Make the finishing phase configurable.

## Requirements & Acceptance Criteria

### Functional Requirements

- API Configuration: The configuration builder/constructor must accept independent curve definitions (e.g., an interface
  or function type (progress: Float) → Float) for Phase 1 and Phase 2.

- Predefined Presets: The library must expose standard predefined curves (e.g., Linear, EaseIn, EaseOut, EaseInOut).

- Input / Output Constraints: Curve functions must map an input progress in the range [0.0, 1.0] to an output value
  in [0.0, 1.0].

- Phase Independence: Setting a custom curve on Phase 1 must not affect the curve configuration of Phase 2, and vice
  versa.

- Validation & Error Handling: If a curve function returns values outside [0.0, 1.0] coerce it into [0.0, 1.0].

### Non-Functional Requirements

**Time-driven Calculation**

- **Requirement:** The progress calculation and curve evaluation must be purely time-driven (a deterministic function of
  elapsed time $t$ relative to duration/ETA) rather than step- or frame-counter-based. Progress at any given
  timestamp $t$ must be identical regardless of sampling frequency.
- **Why it matters:** Ensures consistent animation behavior across different rendering frequencies and avoids
  frame-rate-dependent visual artifacts.

**Performance & Zero Allocation**

- **Requirement:** Curve evaluation and progress resolution must execute in $O (1)$ constant time with zero heap
  allocations per frame/sample to prevent Garbage Collection (GC) pauses and UI stutter during 60/120 fps animations.
- **Why it matters:** Progress animations are evaluated on every render frame; allocating objects inside curve functions
  causes unnecessary GC churn.

**Numerical Stability & Boundary Precision**

- **Requirement:** Curve implementations and the progress engine must guarantee exact boundary values ($f (0.0) = 0.0$
  and $f (1.0) = 1.0$) and avoid precision drift or rounding artifacts due to floating-point calculations.
- **Why it matters:** Floating-point math can result in values like `0.9999999` or `-0.0000001`, which could cause UI
  glitches or prevent completion triggers without proper clamping.

**Pure Functions & Determinism**

- **Requirement:** All predefined curve functions must be stateless, side-effect-free (pure functions), and idempotent.
- **Why it matters:** Ensures predictability, simplifies unit testing, and prevents concurrency issues when multiple UI
  elements observe the progress state.

**Multiplatform Consistency (Kotlin Multiplatform)**

- **Requirement:** Curve calculations and predefined presets must behave identically across all supported KMP targets
  (JVM, Android, iOS/Native, JS/Wasm) without platform-specific floating-point or math discrepancies.
- **Why it matters:** Users expect the animation curves and timing behavior to look identical regardless of the target
  platform.

**Thread Safety & Concurrency**

- **Requirement:** Progress reads, state queries, and curve evaluations must be thread-safe and safe to invoke from
  background threads or UI threads without external synchronization or data races.
- **Why it matters:** Background workers reporting progress or reading status should not conflict with UI render loops
  observing the state.

### Acceptance Criteria

> Define verifiable success conditions using the explicit `Given / When / Then` structure:

Plaintext

```
Given [initial condition]
When [event/action]
Then [observable result]
```

## Context & Constraints

### Existing System Behavior

> Summarize findings from code inspection, verified execution flows, existing data models, and active tests. Clearly
> differentiate between verified facts, assumptions, and unknowns.

### Technical Constraints

> Document non-negotiable limitations (e.g., existing framework versions, external API constraints, persistence rules,
> performance budgets).

### Assumptions & Open Questions

> List assumptions that must be validated, along with open questions, their owners, and resolution deadlines.

## Feature Decomposition & Architecture

### Component Breakdown

> Detail each module or component involved. Ensure each boundary meaningfully reduces coupling or complexity.

| **Attribute**        | **Description**                                 |
|----------------------|-------------------------------------------------|
| **Responsibility**   | > [What this component exclusively owns]        |
| **Inputs / Outputs** | > [Data received and produced]                  |
| **State**            | > [Local or persistent state managed]           |
| **Dependencies**     | > [Internal or external components relied upon] |
| **Failure Behavior** | > [How it handles downstream failures]          |
| **Ownership**        | > [Team or module owner]                        |

### Component Diagram

> Provide a structural diagram showing only components relevant to this feature.

Extrait de code

```
flowchart LR
    Consumer --> Controller
    Controller --> DomainLogic
    DomainLogic --> Storage
```

## Functional Behavior

### Main Flow

> Detail the temporal sequence of the primary happy path using narrative steps or a sequence diagram.

Extrait de code

```
sequenceDiagram
    actor Consumer
    participant Feature
    participant Storage

    Consumer->>Feature: Request operation
    Feature->>Storage: Read/Write state
    Storage-->>Feature: Result
    Feature-->>Consumer: Final response
```

### State Machine & Transitions

> For stateful features, specify current states, valid transition triggers, invalid triggers, side effects, and recovery
> transitions.

Extrait de code

```
stateDiagram-v2
    [*] --> Idle
    Idle --> Processing: Start
    Processing --> Completed: Success
    Processing --> Failed: Error
```

## Interfaces & Data Models

### Interface Contracts

> Define API contracts, function signatures, event formats, or message structures including parameter types, mutability,
> validation rules, and error return types.

### Data Ownership & Persistence

> Identify the single source of truth, read/write access rights, persistence mechanisms, consistency guarantees, and
> field lifecycle.

Plaintext

```
Source of truth: [Component/Database]
Owner: [Service/Module]
Mutable by: [Authorized Actors]
Read by: [Consumers]
Lifecycle & Persistence: [In-memory / Ephemeral / DB Storage]
Consistency Requirements: [Strong / Eventual]
```

### Data Schema Changes

> Outline schema changes, nullability, defaults, or migration requirements. Include an Entity-Relationship (ER) diagram
> if modifying persistent models.

## Failure Analysis, Edge Cases & Concurrency

### Error Behavior & Edge Cases

> Specify handling for invalid inputs, dependency outages, missing data, timeouts, partial failures, and application
> restarts.

### Failure & Edge Case Matrix

> Map failure conditions to expected outcomes.

| **Condition**             | **Expected Behavior**        | **State After**             | **Retryable?** |
|---------------------------|------------------------------|-----------------------------|----------------|
| > [e.g., Network Timeout] | > [Return recoverable error] | > [Rolled back / Unchanged] | > [Yes]        |
| > [e.g., Invalid Input]   | > [Reject request]           | > [Unchanged]               | > [No]         |

### Concurrency & Retry Semantics

> Explain ordering guarantees, race conditions, idempotency mechanisms, synchronization controls, and safe retry
> parameters.

## Alternatives & Decision Records

### Evaluated Alternatives

> Compare at least two viable implementation strategies. Explain why the chosen approach was selected and why
> alternative options were rejected.

| **Criterion**                 | **Option A (Chosen)**   | **Option B**            |
|-------------------------------|-------------------------|-------------------------|
| **Implementation Complexity** | > [Low / Medium / High] | > [Low / Medium / High] |
| **Performance**               | > [Impact assessment]   | > [Impact assessment]   |
| **Backward Compatibility**    | > [Risk assessment]     | > [Risk assessment]     |
| **Maintenance Cost**          | > [Long-term cost]      | > [Long-term cost]      |

### Architecture Decision Record (ADR)

> Record significant or irreversible technical choices.

Markdown

```
Context: [Why a decision is required]
Decision: [Chosen approach]
Rationale: [Why it was selected over alternatives]
Consequences: [Trade-offs, limitations, and future obligations]
```

## Risks & Mitigations

> Identify technical, operational, performance, or timeline risks along with explicit mitigations.

| **Risk**                            | **Probability**  | **Impact**       | **Mitigation Strategy**        |
|-------------------------------------|------------------|------------------|--------------------------------|
| > [Specific technical failure mode] | > [Low/Med/High] | > [Low/Med/High] | > [Concrete mitigation action] |

## Verification & Observability Strategy

### Testing Plan

> Detail test plans across mandatory quality tiers:

- **Happy Path:** > [Primary validation strategy]
- **Boundary Conditions:** > [Minimum/maximum values, empty collections, lifecycle limits]
- **Failure & Edge Cases:** > [Dependency failure, unexpected input handling]
- **Concurrency & Race Conditions:** > [Parallel execution checks]
- **Backward Compatibility:** > [Contract preservation checks]

### Observability

> Define required metrics, log events, distributed tracing, or diagnostic endpoints necessary to detect and debug issues
> in production.

## Review & Completion Criteria

### Design Review Severity

> Use structured severity tags when resolving review feedback:

- **Blocking:** Must resolve before implementation starts.
- **Major:** Significant technical, compatibility, or operational concern.
- **Minor:** Non-blocking improvement.
- **Nit:** Stylistic or optional suggestion.

### Handoff Readiness Checklist

> Confirm all sign-off requirements are satisfied prior to coding:

- [ ] Problem, scope, and non-goals are explicit.
- [ ] Requirements and acceptance criteria are agreed upon.
- [ ] Existing system constraints and behavior were verified.
- [ ] Interfaces, data ownership, and component boundaries are defined.
- [ ] Edge cases, error matrices, and concurrency rules are documented.
- [ ] Viable options were evaluated and trade-offs recorded.
- [ ] Testing and observability strategies are defined.
- [ ] All Blocking review comments are resolved.

## Implementation Handoff & Maintenance

### Task Breakdown & Rollout

> Map implementation work into small, reviewable milestones. Specify feature flags, rollout stages, migration steps, or
> fallback switches.

### Design Deviations & Post-Launch

> Document material deviations from the approved design during implementation, along with post-launch findings and known
> limitations.   
