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
└── pom.xml
```

## Getting Started

### Prerequisites

- JDK (version 21 or higher)

### Running the Project

**Windows:**

```bash
gradlew runClient
```

**macOS / Linux:**
```bash
./gradlew runClient
```

### Building the Project

```bash
gradlew build
```

**macOS / Linux:**
```bash
./gradlew build
```