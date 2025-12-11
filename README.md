# Software Patterns Project: TextEditor
**Author:** Alexandre ONCINO

My project of Software Pattern is a text editor contains 3 patterns (Singleton, Strategy, Observer).

These patterns help achieve 'Clean' code through:
  - Centralized data management (Singleton)
  - Flexibility in saving formats (Strategy)
  - User interface responsiveness (Observer)

**1. The Singleton** (Clipboard Class)

Concept: Ensures that a class has only one unique instance and provides a global access point to it.

Use Case: The clipboard is inherently a singular, shared resource. Having multiple active clipboards simultaneously would be illogical and lead to data inconsistency.

Implementation: By using a private constructor and a static getInstance() method, the application guarantees that the exact same object is retrieved for copy/paste operations, regardless of where it is called in the code.

**2. The Strategy** (ExportStrategy Interface)

Concept: Defines a family of algorithms, encapsulates each one in a separate class, and makes them dynamically interchangeable.

Use Case: This avoids messy if (type == WORD) ... else if (type == HTML) chains. It adheres to the Open/Closed Principle: adding support for PDF or Markdown in the future only requires creating a new class, without modifying the existing editor engine.

Implementation: The editor utilizes a setExportStrategy() method. When the user selects a format from the dropdown (Word or HTML) and clicks "Save", the editor delegates the task to the active strategy, abstracting away the specific file-writing logic.

**3. The Observer** (StatsObserver Interface)

Concept: Establishes a "one-to-many" dependency: when the state of one object (the Subject) changes, all its dependents (Observers) are automatically notified and updated.

Use Case: This decouples the core logic from the user interface. The text engine remains unaware of the specific Swing components; it simply broadcasts that a change has occurred, allowing the interface to react independently.

Implementation: As soon as a character is typed, the TextEditor (Subject) notifies the WordCounterLabel (Observer). This ensures the word and character counts update smoothly in real-time.
