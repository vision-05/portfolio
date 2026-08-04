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

MAYBE ADD POWER BUDGET

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
I began with a 29.5x63.5mm rectangle. The aim was to have as small of a footpring as possible while successfully routing all of the components, minimising EMI, noise and having good thermal design.

#### Noise considerations
My main noise considerations come from the motors/battery and the switching converter. In our case motor signal is carried by UART so unlike a PWM signal it is not as much of a noise inducer. The motor power itself and the battery are higher voltage, and also unregulated supply, so we try to keep that away from the logic components

#### Rough placement
The 12V nets are placed on the right hand side of the board and the 3.3V are placed on the left hand side. The motor connectors are positioned in the top middle of the board, with the board in and jetson out power connectors on the bottom right. The IMU is placed in the top left, furthest from the noisy switching converter, and the MCU is on the left hand side of the board. The CAN and UART connectors are also placed on the left hand side. The Jetson power connector was more of an afterthought (unfortunately these things slip your mind sometimes), so I extended a portion of the bottom right corner to accomodate it, making the width on the right of the board 41mm instead.

#### Split power plane
The left hand side has a 3.3V power plane, for logic, while the right has a 12V power plane for the higher powered componenets. The 5V net is a short, thick trace between the output of the buck converter and the input of the LDO, somewhere in the middle of the board.

#### Screw terminal connectors
Screw terminal connectors were chosen as they are very compact for their current carrying capabilities. Standard screw terminal connectors (5mm pitch) are rated for around 15A, which is just about at the upper side of our current budget. The disadvantage however is their susceptibility to reverse polarity, as it is a reversible connector. Furthermore, we would find out making 14AWG wire ends that fit in the connector to be a time consuming, painful process, especially without a proper crimping tool.

#### UART headers
For UART I decided standard 2.54mm pitch header pins was the way to go. While this isn't a bad choice, for a next design I'd probably opt for a JST GH or even SH, as this would be locking and slightly more secure.

#### XT GH for CAN bus
I went with a non-reversible locking connector for the CAN bus connector. While Molex connectors are more expensive, they're certainly worth it for components such as motors that you don't want unplugging randomly during operation. The JST GH is perfect for the CAN bus as it doesn't carry much current, so the small form factor is very convenient. You can also get female connectors for relatively cheap with pre-crimped wires, so making your own connector is easy.

#### Molex connectors for servo
For ease of use, I chose the board connectors that the waveshare ST series drivers already use. The female end (wires) use the SPOX mini 5264 3 pin connector, so I found the equivalent male board connector, the 5267. These can carry up to 3A per connection, which is perfect for motors that stall at 2.7A. Because of the likelihood of a stall, I gave each motor its own connector so we wouldn't have to worry about more current going through a contact and damaging the connection.

#### Status LEDs
I opted for 2 status LEDs, one for the 12V net and one for the 3.3V net, to show that power was correctly biased and reaching the correct components.

### Phase 5 - DFM check and manufacture

#### Silkscreen
In terms of DFM, I make sure that I avoid putting vias in pads if possible, and give as many components as possible good silkscreen labels. In terms of our end use, it is very important to label the connector pins/orientations. I skipped this on the power inputs and this later proved to be a costly mistake.

#### Final BOM

| Comment | Designator | Footprint | LCSC | Quantity |
| --- | --- | --- | --- | --- |
| 0.1u | C1,C10,C11,C12,C2,C4,C9 | C_0402_1005Metric | C307331 | 7 |
| 1.37k | R4 | R_0402_1005Metric | C159124 | 1 |
| 1.8k | R12 | R_0402_1005Metric | C25871 | 1 |
| 100 | R13 | R_0402_1005Metric | C25076 | 1 |
| 10k | R10,R3,R5,R6,R7 | R_0402_1005Metric | C25744 | 5 |
| 10k | R8 | R_0603_1608Metric | C25804 | 1 |
| 10n | C3 | C_0402_1005Metric | C15195 | 1 |
| 120 | R9 | R_0402_1005Metric | C25079 | 1 |
| 1u | C6,C7 | C_0402_1005Metric | C52923 | 2 |
| 3.3k | R11 | R_0402_1005Metric | C25890 | 1 |
| 4.7u | C5 | C_0402_1005Metric | C23733 | 1 |
| 470u | C8 | CP_Elec_8x10.5 | C26236113 | 1 |
| 68.1k | R1 | R_0402_1005Metric | C11537 | 1 |
| 9.53k | R2 | R_0402_1005Metric | C96273 | 1 |
| Conn_01x04_Pin | J6 | JST_GH_BM04B-GHS-TBT_1x04-1MP_P1.25mm_Vertical | C161692 | 1 |
| Conn_ARM_JTAG_SWD_10 | J10 | PinHeader_2x05_P1.27mm_Vertical_SMD | C448647 | 1 |
| ECS-TXO-2016-33-160-TR | XTAL1 | TXO-2016_ECS | C2451469 | 1 |
| FerriteBead | FB1 | R_0201_0603Metric | C5159964 | 1 |
| LED | D1,D2 | R_0402_1005Metric | C130723 | 2 |
| LSM6DSV16XTR | U5 | LGA-14L_STM | C5267406 | 1 |
| SMAJ13CA-13-F | CR1 | SMA_DIO | C134951 | 1 |
| SQJ461EP-T1_GE3 | U6 | SO-8L_VIS | C3279500 | 1 |
| STM32G4A1KEUx | U1 | QFN-32-1EP_5x5mm_P0.5mm_EP3.45x3.45mm | C3225626 | 1 |
| TJA1051T_3 | U2 | SOT96-1 | C58988 | 1 |
| TPS7A0533PDQNR | U4 | Texas_X2SON-4_1x1mm_P0.65mm | C2870713 | 1 |
| Conn_01x03_Pin | J3 | MOLEX_22035035 | — | 1 |
| Conn_01x03_Pin | J4 | MOLEX_22035035 | — | 1 |
| Conn_01x03_Pin | J8 | MOLEX_22035035 | — | 1 |
| RPX-2.5-CT | U3 | RPX-2.5 | — | 1 |
| Conn_01x02_Pin | J9 | PinHeader_1x02_P2.54mm_Vertical | — | 1 |
| SW_Push | SW1 | SW_PUSH_6mm | — | 1 |
| Screw_Terminal_01x02 | J1 | TerminalBlock_Phoenix_MKDS-1,5-2_1x02_P5.00mm_Horizontal | — | 1 |
| Conn_01x03_Pin | J2 | MOLEX_22035035 | — | 1 |
| Conn_01x03_Pin | J5 | MOLEX_22035035 | — | 1 |
| Conn_01x03_Pin | J7 | MOLEX_22035035 | — | 1 |
| Screw_Terminal_01x02 | J11 | TerminalBlock_Phoenix_MKDS-1,5-2_1x02_P5.00mm_Horizontal | — | 1 |
 

### Phase 6 - Verification
On receiving the boards it is important to have a good bringup process

#### First power - supply
If you want to avoid smoke, fires and blown fuses, it is a good idea to start by powering your board through a power supply, in a current controlled mode.

Before even plugging in, inspection with a multimeter is a good idea. Using the continuity mode, check that the ground nets are all ground, and that other nets do not short to ground around the board. If you are unsure of polarity, double check with this. With a functioning reverse bias protection MOSFET, the VBat_in terminal should not be on the same net as the +12V in the board, when there is no power supplied. Then with a moderate current, enough to power the MCU at least (0.2A maybe), power on the board. If LEDs are on and there is no smoke, you are probably good. If you have any issues at this point, do some debugging by turning down the current limit even lower (0.1A) and you can check which components are getting hot with some isopropyl alcohol or a heat gun. If everything is going alright, you can switch away of increase the current limit.

Next, on a low current, again check the reverse bias protection. Reverse the power connector and watch... the power shouldn't turn on, and nothing should smoke or get hot either. The circuit should be open. Verify with a multimeter that the power is not going past the MOSFET.

After this, I tested that we could power the Jetson at the same time. I had cut the ends off a DC barrel jack connector, tested the nets with a multimeter and soldered on the bulk capacitor, before inserting it the correct way into the second screw terminal. This time, with a 1A current limit I powered it on, and the Jetson's status LED started up. I was able to SSH in, and could see the Jetson was powered at the same time.

#### Communication functions
The next step was to check I could speak to the Jetson from the board and vice versa. Unfortunately due to the Jetson being borrowed, I couldn't solder the CAN headers on so UART was now our main communication channel. I used the Jetson's bare IO to probe the UART connnection once wired up and confirmed pings were being received. Unfortunately it turned out there was a bug in the Jetpack firmware, where DMA UART receive was not working at all, so we had to switch to a high baud rate polling instead.

#### Motor driving
I then tested a single motor plugged in, driving it through a test script that just sent a motor command over UART from the board. I realised I had missed out on putting a pull up resistor on the UART connection, meaning the signal was not being sent properly. I believe in the half duplex mode the UART operates in an open drain fashion for these motors. Luckily, the STM32s are robust and we were able to drive by operating the UART in a push pull mode instead, as motors are addressed one at a time anyway.

#### Battery power
Once all functions are confirmed, it is probably safe to power via the battery. After making sure it is charged, it is plugged in and we confirmed that both the board and the Jetson were successfully running on battery power.

### Phase 7 - Final firmware

#### Micro ROS
In order to work seamlessly with the code authored for our navigation and control stack, as well as the main robot and simulation code, the control board was loaded with micro-ros, using UART as the transport medium. We published to /imu/aux and a battery voltage channel, as well as implementing PID controller for motor commands. Odometry was also published from the micro ros node.

#### OTA Programming
Over the Air programming was implemented. Using the Jetson, a UART programming bridge was set up in the firmware, through a ROS topic that could be sent the compiled binary code to be flashed. The firmware could then flash the new program and reboot into the newly-installed firmware. Because we would SSH into the wifi connected Jetson, this would act as an over the air programming interface.

#### Motor ID assignment
In order to use the motors individually, as 6 were on one single bus, we had to assign the proper unique IDs, which involved writing a program that scanned the bus and read IDs, and then sent the command to program a new ID to a motor. Then, plugging the motors in one by one, they were given IDs from one to six and mapped to their respective positions.

## Engineering Analysis

### Successes
The board worked perfectly. There was never any need for a v2 and besides one accidental board destruction (not from design), the final board completed the mission objectives and stayed intact and functioning even after a pyroshock test (simulating the effect of rover entering orbit in spacecraft).

### Improvements to make
#### Connector polarity
The screw terminal connectors were not marked for polarity, meaning one fateful day the polarity was easily reversed. Luckily our P channel MOSFET protected the circuit from this, but the screw terminals had another issue, where one of them actually broke because of the tension from the thick 14AWG wires feeding in pulling up and causing the contact to break.

#### Connector choice
To alleviate this, a smart choice would have been a right angled XT30 through hole connector, which solves both the polarity issue and structural integrity, while keeping in a small form factor for both the power to the Jetson and from the battery.

#### Full spice simulation
The board was designed with many calculations and consultations to datasheets, bode plots and overall care, but I chose not to do any in depth studies on interference between components and signals, parasitics and the like. This was necessary because of the short time frame I had to make the board (1 week design + 1 week for manufacture), although for completeness and best engineering practice it would've been a good idea.
