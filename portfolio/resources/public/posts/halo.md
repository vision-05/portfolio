# Project brief
HALO or Holistic Agentic Living Orchestration, is a fully local, agentic smart home system. It is privacy first, proactive and has the capability to run fully locally, even on resource constrained hardware.

This was a project I undertook in my 2nd year of Robotics and AI MEng at UCL, where I worked as the Lead Architectural Engineer. The total duration of the project was around a single term, so 12 weeks give or take including a 4 week holiday period.

Smart homes today have at least one problem. They are fragmented, reactionary or cloud based. Smart devices are built by different vendors who want to lock you into their ecosystem. You need a different app for each device, they seldom talk to each other and the ability to coordinate and orchestrate them is hard to come by. People need to be technical in order to make any useful automations to their lives, and the current market state is simply not accessible, or privacy oriented.

There has been a change, recently with OpenClaw, although again, this is a general purpose LLM based orchestration system, and to run it safely, the user has to manually implement safeguards and monitor it, or sandbox it. Again, this is fine for the developers, but it doesn't make a marketable system to the general population, where they might wake up one day to their computer files being removed, or other kinds of malfunctions and hallucination based issues.

## Project Overview
We have developed a fully working HALO proof of concept, successfully implementing 4.5/5 of the pillars successfully. You can talk to the system through Telegram and it can answer general questions, give recommendations on things to do locally, prepare for you going out, control your TV, start a movie or games night, put events in your calendar, and more.

## Design philosophy
The system has been designed in an event driven, actor based methodology. This is a personal choice of paradigm that I have been using for years, and have found it very effective for making systems that humans interface with feel alive, progressive and robust. I use the term actor based because the word agentic or agent based have been re-defined by AI agents, although in the world of Clojure, where I do a lot of development, the concept of the asynchronous primitive of an agent has existed for years before it became a mainstream, LLM related term.

The core of the HALO structure is a system of logically separated agents. Agents are on the most part "unintelligent", meaning they lack their own proactivity system. They are agents in the sense that they act like their own programs, doing some form of reaction to messages they receive, with the ability to send new messages or forward existing ones. It is also possible to have intelligent agents, and they show up as an integral component of the original HALO proof of concept. These intelligent agents have the ability to be proactive, which is a feature I will discuss in more detail later.

Agents should talk to each other in a way that is auditable and interprable by humans, and be able to distribute the completion of tasks between a network. The network of agents therefore can be extendable, asynchronous, capable of redundancy and load-balancing as standards, not as afterthoughts.

## Pillars of HALO
After extensive reading on the topic of smart homes and looking into current solutions, we decided to name the important pillars upholding the HALO project.

- ### Anticipation
- ### Negotiation
- ### Actuation
- ### Initialisation
- ### Extension

Of course there are many more underpinning principles and requirements, to make this move away from a student project and make it actually usable, but in the entire project the group aimed to make something that was not just proof of concept, but something that was just a step away from being production ready for real users.

### Anticipation
Without the ability to anticipate, systems whether inanimate or living, are purely reactionary. They have no ability to pre-empt or be proactive, and so they don't seem as smart or capable compared to humans. This is a key area that many smart home systems lack. Even with an abundance of sensors and AI intelligence, systems like Amazon Alexa require humans to create pre-made routines with very specific triggers, offering very little flexilibity in the narrow-scope of proactivity their systems do offer. During our "research" phase, there was very little evidence of other anticipatory smart home systems existing, even including OpenClaw based solutions.

### Negotiation
Negotiation is the act of agents talking to each other, more specifically coming to an agreement on a policy, given their potentially competing interests. Negotiation is important in any system where we want to optimise for multiple objectives, especially when multiple objectives are distributed between different actors. In the smart home, this may show up in scheduling the use of a washing machine, deciding the temperature of a room, power routing for optimal cost savings or environmental efficiency, the list goes on. There are many ways to negotiate mathematically, but in the context of a home, with living people, there has to be a way to negotiate qualitatively to achieve optimal results. This also means that a metric to measure negotiation quality is difficult to derive, other than to discover it empirically, through repeated trials of the negotiation system. We did formulate a simulation to attempt this, but no simulate can truly reflect the outlook of real people.

### Actuation
Actuation is the verb describing the cause of operating a machine. The utility of an actuating system in a smart home should be obvious, but it is vital that this is an integral part of our system. We are building a system with actuation as a built in function, rather than an extension of the system. This differs us from tools like OpenClaw or other LLM agents with MCPs attached. This is also one of the trickier problems to solve, as every device operates differently and with different rules.

### Initialisation
Initialisation describes the problem of setting up and starting up a smart home system. In the past, it has been a tedious process, involving lots of pairings of devices, writing rules and routines manually. We aim to make a more approachable, universally accessible initialisation system, therefore we are designing the initialisation system thoroughly as a core part of the our HALO project. While we can't get rid of all of the device pairing, we can make the rest of the network startup be as easy as saying in plain language what you would like the smart home to do.

### Extension
The final pillar, extension, refers to the ability to add to the system and create new functionality, without modifying the core. The HALO system is designed to be extremely easy to add new functions, especially for non-technical users, with the assistance of Large Language Models. Without extensibility, the system would be locked to the features the initial creators developed, and especially with smart homes where products are added and updated regularly, it is important that making a new or updated device work is frictionless.
