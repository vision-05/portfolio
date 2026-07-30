# MarshGazers

## Brief
I worked in a team of 10 to create a Mars Rover for the 2026 UK SEDS ORTS competition. Our entry won best CDR, which translates to the best design.

I was on the autonomy team, but my main contribution was the final electrical subsystem, most notably the control and power delivery PCB as well as the low level firmware.

## Mission Statement
The 2026 Mission was a soil sampling mission: your rover must go to 3 sites and collect sand samples from the ground, returning the samples to a collection point. There is a 30 minute total time limit, and manual interventions such as re-righting the robot, repairs, etc are all points deductions.
An additional challenge was to achieve the highest level of autonomy possible. In this case we chose to attempt full autonomy.

## General design

## Electrical Subsystem

### Phase 1 - Perfboard and General Requirements
The initial electronics design was laid out by my teammate, who worked on the electronics that we used for the majority of our prototypes. The requirements for our electronics system are a few fold:
- Take power from 12V 3S LiPo battery
- Power an NVidia Jetson Orin
- Power up and control up to 6 ST3020 servo motors simultaneously
- Kill switch for instant power off

These 4 requirements are all we need to get our mission completed successfully. The initial electronics system therefore was made up of:
- FPV drone power delivery board
- Arduino Giga R1 Wifi
- 2 Waveshare ESP32 motor driver modules
- 6 ST3020 Servo motors
- Kill switch
- Nvidia Jetson Orin Nano

We powered the Jetson through the wall for this setup. There was also an additional perfboard that acted as a power rail.

This system worked fine, but had cables everywhere. The mechanical team attempted to design 2 versions of the enclosure, one with the initial setup, and one provisioned for the "to be determined" PCB, but it ended up that with all of the components above, only a small form factor PCB would be able to do the job.

### Phase 2 - PCB requirements
The requirements here are much greater, as we are doing more work from scratch:
- Control 6 ST3020 Servo Motors
- Power Jetson Orin Nano through battery supply
- Kill switch for instant power off
- Step down 12V to 3.3V logic for low level controller
- Micro ROS node for low level control
- auxiliary IMU on low level controller
- Smooth voltage spikes for power delivery
- Provide 2 communication channels between MCU and Jetson

The other main factor I had to consider was dimensions, as this had to be compact enough to fit in the small, lower compartment of our chassis. I ended up with a dimension of 60x41mm, so slightly smaller than an Arduino Uno form factor. As it turns out, the board could have been doubled in size.

For the purposes of ease of design, manufacture and repairability, I chose to go for a 4 layer board with single sided assembly.

### Phase 3 - PCB schematic capture
![](out/marshgazers.svg)

I begin the circuit design by going in reverse from the end goal. The requirements state we need to drive 6 ST3020 Servo motors, talk to the Jetson by 2 robust methods, run MicroROS and talk to an IMU.

Starting with the servos, we need to look at how they are driven. According to [WaveShare site](https://www.waveshare.com/wiki/ST3020_Servo?srsltid=AfmBOorIegTQRR6WXdnMb1lv5_sHYj1AtUHmExvoQsjfnhRMfnpFB-jI) these servos have their own drivers internally, and simply receive commands over a shared half-duplex (Tx only) UART bus. Servos have 8 bit IDs, so up to 253 can be chained together on the same bus for signal. This makes our lives easier, as we can use a single UART Tx pin for sending signals to all of our motors. Our MCU therefore will need 1 UART Tx pin.

Next we look at communication. The gold standard for communication between modules in autonomous, especially driving systems is CAN bus. This stands for Controller Area Network. It is great for 2 main reasons: it is simple, and it is robust. CAN bus only needs 2 wires in a differential pair, and many devices can all talk on the same bus so it is easily hot pluggable. Being a differential pair, with a twisted pair of wires we get a very robust connection which is able to handle more external noise without corrupting our signal. This is our primary connection. This means we need some kind of CAN transciever module, as well as CAN_FD pins on our MCU.
The Nvidia Jetson has support for CAN bus, although headers are not soldered by default and it also requires an external transciever.

Our fallback will be a UART connection. This comes with 2 benefits: "OTA" programming as well as our communication channel. The Jetson has UART headers exposed in its 40 pin header, so it is easy to wire up. You can also program a microcontroller chip over serial interface, meaning that with the Jetson powered on and wired up to the controller, we can wirelessly communicate with the Jetson to program the MCU with firmware, even though the board itself doesn't have wireless connectivity.
This means we need an additional pair of UART Tx/Rx pins.


Finally, for our IMU we select an IMU with SPI communication (usually they also are dual-mode with I2C), although I2C is not as robust or fast of a connection, so we choose SPI. In this design it will make 0 difference however.

Then given the MicroROS requirement, we need an MCU that has at least 50kB of RAM to provide enough heap space for MicroROS with any other firmware memory.

#### MCU

This led me to the choice of the STM32G4A1KEU6. This is probably the smallest, lowest powered STM32 chip that supports MicroROS, with Arm M4 Cortex core at 170MHz clock. It comes in a smallest package of 32-UFQFPN which is what we chose to use, being 5mmx5mm. It has multiple UART, SPI, and CAN_FD peripherals, which means it supports all of our requirments, and gives us a good number of pins in a very small form factor. The cost for this part was about £5 at the time of manufacture.

#### Voltage Regulation

Then from the MCU choice we can work out the MCU's power requirements. The STM32 chips generally operate between a 2.7V-3.7V range, and we have a Battery supply voltage anywhere from 9V (absolute minimum) to 12.6V. This means we have to step down voltage, and especially because we are using an analog pin on the STM32, we have to make sure we have a stable input/reference voltage. 

In general, there are 2 types of voltage regulators, linear and switching. Linear regulators are clean and stable, but they use resistance to drop voltage, and therefore generate lots of heat if high voltages need to be dropped. This also means efficiency is relatively low, on the scale of 40% for dropping 12V down to 5V. On the other hand, switching regulators use a duty cycle, similar to the concept of PWM, to make an average lower voltage, with much less loss to heat, giving potential efficiencies of 90% for the same voltage drop. However, because of the switching nature of these regulators (and often they need inductors which can add to parasitic effects and EMI), they are very noisy and can intefere with other signals.

For these reasons I chose a 2 stage step down configuration, with a switching buck converter to drop battery voltage to 5V, and then a Low Dropout (Linear) Regulator, to step down from the 5V to the 3.3V logic level. This mode can also be quite good if you have certain devices that require a 5V logic level as you have built in this capability into your board by default.

The specific regulators I chose were the RPX 2.5 (my normal go to) and the TPS7A0533PDQNR.
I usually go with the RPX 2.5 module as it is a complete module, shielded with a built in inductor in silico. This means I don't have to add an external inductor to the circuit and noise becomes less of an issue as it is already shielded. The TPS7A0533PDQNR was chosen due to its small form factor, being in a 4-X2SON package, and its low voltage dropout of 0.235V. Furthermore it provides enough current (200mA) for all of our logic needs.

#### CAN transceiver
I chose the TJA1051T CAN transciever, the 3.3V variant. It is in an SOT-8 form factor so is reasonably small. It does the exact job needed and costs £1.40

#### IMU
For the IMU I selected an LSM6DSV16X 6 axis IMU. It is expensive but also high quality, and has an SPI or I2C interface. Importantly, it has the option for operating at a low power mode, running its own embedded AI models for stuck-detection or terrain classification and has the ability to run its own control loop with an embedded state machine, if you desire to offload some data processing. It also has very good internal filtering.

#### Battery Voltage Sensing
For safety reasons, and mission control, we chose to measure the battery voltage throughout. The system then shuts off when the battery reaches a 9.6V threshold, so the LiPo never reaches the discharge limit of 3V per cell (9V total voltage). I chose the simplest schema, a voltage divider feeding into an analog to digital converter pin on the STM32. The voltage divider limits the maximum input voltage as a ratio of the maximum battery voltage, so we do not cause damage to the STM32 with higher than tolerable voltage levels. The choice of resistor heavily impacts the accuracy of this reading. With +-10% resistor accuracies, in our voltage range there was a roughly 0.1V persistent error after calibration, but we determined that this is OK, as it is roughly 0.3% of our total range, and insignificant for the purpose of our threshold.

#### Transient Voltage Surge Protection
When powering a circuit by a battery with a fast discharge rate, such as our LiPo battery, often time sudden loads can cause momentary, sudden peaks in voltage that are higher than the battery's maximum voltage, much like how the battery can do the reverse by sagging under sustained loads. Without TVS protection, our converters or other components may receive too much voltage suddenly and become damaged. One protection we have employed for our capacitors is making sure capacitor voltage ratings are 2x their expected load (25V for a 12.6V supply, less is OK for the 3.3V powered section). We can use a Transient Voltage Surge protection diode, which acts as an open circuit, until the voltage reaches a threshold, and then it acts as a short to ground for any additional voltage.

I used the SMAJ13CA-13F TVS diode, with a 13V reverse standoff voltage and 14.4 to 15.9V breakdown voltage. The max clamping voltage is 21.5V, meaning any transient voltage between 14.4-15.9V and 21.5V will be shunted to ground. Note that these devices can't withstand sustained surges as they have a significantly lower steady state power dissipation as opposed to their peak pulse disippation.

#### Reverse Bias Protection
When designing a circuit, there is often a risk that somehow, power and ground are reversed. Some components have built in protection, but you often don't want to rely on this entirely. The two main solutions are asymmetric/one way connectors, or reverse bias protection. One way connectors guarantee that only one orientation of wire configuration can contact the board, but it still relies on the proper creation of connector cables to make sure the right nets are connected to each other. It is still possible to damage a board with one way connectors, if for instance ground and power are swapped from the standard positions in a connector, or custom connectors have been fabricated. Therefore, the foolproof method is to use a P channel MOSFET.

Diagram here.

This device acts like an open circuit when biased in one direction, and a short circuit in the other. You should place one of these in front of any connector before any other nets are reached, or at the very least in front of any connectors where polarity may be flipped.

I opted for the SQJ461EP P channel mosfet as it has very high voltage and current ratings, and our maximum projected current draw could be as much as 15A.

#### Bulk Capacitors
Bulk capacitors exist to a) supply additional voltage to components when they draw suddenly and need quick extra voltage or b) act as a reservoir when supply voltage sags. In general it has the effect of smoothing out transients, absorbing extra voltage from source and then supplying when needed. I decided the main circuit would suffice with a single SMD 470uF electrolytic capacitor. This proved to be plenty for all of the motors. This was OK because the LiPo has a very fast discharge rate and is often OK to supply current quickly, unlike other types of battery. However for the Jetson board, we needed to make sure it had its own reservoir, as its current draw can be quite high (up to 5A surge, 3A nominal), thus I used a 1000uF capacitor, this is mainly to avoid brownouts if there is a case that the voltage sags significantly when the motors are under heavy load. It turns out that this configuration worked perfectly and we never had any unexpected stalls or shutdowns from any components.

#### Decoupling Capacitors
Decoupling capacitors act as filters to your power supply, with the aim of removing noise so your reference voltages are stable. Without these, microcontrollers and other active components may brownout, or reset due to temporary voltage sags below operating voltage. Things to consider: 0.1uF standard is outdated, higher capacitance reduces the cutoff frequency, meaning a stronger filtering effect, however reaching very high capacitances means larger, more expensive parts. In general you want to use small footprint MLCC capacitors.

The circuit of a decoupling capacitor should be the shortest path between the pin where power is supplied to and the ground near it. This reduces parasitic inductance, which has the opposite effect of capacitance in a decoupling circuit. In this way, smaller footprint capacitors are better, but keep in mind that MultiLayer Ceramic Capacitors suffer from a phenomenon called DC bias derating, where the effective capacitance is lowered when a constant voltage is applied across it. Higher voltage causes higher derating, but higher voltage rated capacitors are less affected by this. Thus at 12V for instance, an 0402 1uF capacitor may actually only have 40% of its capacitance available, so act as a 0.4uF capacitor. Larger footprint capacitors will derate less, but then they take up more space and give a longer circuit for ESL to impact decoupling. I typically choose 1uF 0402s from Samsung. You can check the capacitor datasheets to see the derating characteristics and view the Bode plots for their filtering frequencies.

Furthermore, for analog circuits this may not be enough. Typically for analog supply voltage, it is also separated by a ferrite bead, which is essentially a frequency dependent resistor. An ideal ferrite bead increases impedance with frequency, but in the real world with both parasitic capacitances and inductances, there is a more capacitive impedance, then a peak where the two are equal, and then a more inductive impedance. The characteristics, such as amount of ohms of impedance and bode plot for attenuation can wildly differ, so ferrite beads need to be chosen carefully with regard to the signals encountered in the circuit. With more lower frequency signals, it might be more important to pick a ferrite bead with higher resistance in the lower frequencies, whereas in high speed designs, high speed noise will dominate and a stronger peak in the MHz would be more important.

#### Omission of per motor current sense
Per motor current sense was considered, although was ultimately not decided to be too useful as the motors we use already have built in feedback. It made no operational difference and saved on a little bit of complexity in our design.

#### Omission of individual motor bulk capacitors
It could have been more proper to add bulk capacitors for each individual motor, however qualitatively there was no need, leading to a saving of space in our board.

#### Omission of power regulation for Jetson
Best practice would be to power the Jetson with a constant voltage level, say 12V. The minimum accepted voltage is 9V, which is lower than what our battery would be supplying, thus we decided there is low risk of a brownout. There could be the case of a voltage sag, but then this would probably cause bigger issues to the battery if it did sag below 9V, and furthermore, the rather large 1000uF capacitor on the power supply to the Jetson was able to handle the transient demands just fine in practice. I did however design a second power board for the Jetson, with a buck boost converter (and audio module, just for fun), however the spec of buck-boost is relatively high and the chip alone cost £15, being more expensive than any other individual part in the BOM. Thus, this would also be cost saving to leave out, beyond size, time, complexity, etc.

### Phase 4 - PCB layout design
![](out/marshgazers-F_Cu.svg)
![](out/marshgazers-In1_Cu.svg)
![](out/marshgazers-In2_Cu.svg) 
![](out/marshgazers-B_Cu.svg)

#### Edge footprint

#### Noise considerations

#### Rough placement

#### Split power plane

#### Screw terminal connectors

#### UART headers

#### XT GH for CAN bus

#### Molex connectors for servo

#### Final placement

### Phase 5 - DFM check and manufacture

### Phase 6 - Verification

### Phase 7 - Final firmware

## Engineering Analysis

### Successes
The board worked perfectly. There was never any need for a v2 and besides one accidental board destruction (not from design), the final board completed the mission objectives and stayed intact and functioning even after a pyroshock test (simulating the effect of rover entering orbit in spacecraft).

### Improvements to make
#### Connector polarity

#### Connector choice

#### Full spice simulation
