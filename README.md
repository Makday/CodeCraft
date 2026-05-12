<div align="center">
  <img src="./logo_name.svg" alt="CodeCraft" width="600"/>
</div>

## About

**CodeCraft** is a DSL (domain-specific language) that lets you script and automate actions in Minecraft. Developed by students of Software Engineering at the Technical University of Moldova:

- Chiril Boboc — [@KyronPomidor](https://github.com/KyronPomidor)
- Vasile Brînză — [@Kynexi](https://github.com/Kynexi)
- Cristian Bruma — [@Makday](https://github.com/Makday)
- Gabriela Bîtca — [@gabr1ela0](https://github.com/gabr1ela0)
- Teodor Strulea — [@Strulea-Teodor](https://github.com/Strulea-Teodor)

## Repository Structure

```
codecraft/
├── src/                  # Maven standard layout (main & test sources)
├── docs/
│   ├── week_1/           # Progress reports by week
│   ├── week_2/
│   └── ...
│   └── week_x/
│   └── Report/           # LaTeX source + compiled PDF report              
|   └── code_examples/    # Examples of the code written in the DSL
├── documentation.md      # Language documentation
└── pom.xml
```

---

## Getting Started

1. [How to run the project](#running-the-project)
2. [How to build and use the project](#building-the-project-and-usage)
3. [Language documentation](documentation.md)

---

### Running the Project

### Prerequisites

- JDK (version 21 or higher)

**Windows:**

```bash
gradlew runClient
```

**macOS / Linux:**
```bash
./gradlew runClient
```

---

### Building the Project and usage

### Prerequisites

- JDK (version 21 or higher)
- Minecraft (version 1.21.6)
- Fabric loader for Minecraft (version 0.18.5 or higher)
- Fabric API

**Windows:**

```bash
gradlew build
```

**macOS / Linux:**
```bash
./gradlew build
```

After building, you can find the generated JAR file in the `build/libs` directory. 
To use the mod, place the JAR file in your Minecraft `mods` folder.
Make sure your Minecraft installation is set up to use the Fabric loader.
Additionally, make sure the `mods` folder contains the Fabric API JAR file, which is required for the mod to function properly.

