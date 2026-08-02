# Cart pole kit

## Project Statement {.glow}

Design and manufacture a free moving robot.

The base robot can balance an inverted pendulum in 1 and 2 dimensions

The robot can be extended as a general purpose differential drive robot with autonomous navigation capabilities

## Constraints

Initial base robot should cost no more than £60 to produce

Pendulum should be 60cm long and have an attached mass of 50g

Robot should have multiple control capabilities:

- Onboard, online MPC
- LQR
- Pole placement
- Off board reinforcement learning
- PID
- Sliding mode

Robot extension module should cost no more than £150 to produce

Extension module should have the following capabilities:

- Online SLAM EKF
- LLM support at low frequency
- Path planning

Robot should run for 15 minutes minimum before needing recharging. Extended by secondary battery modules if possible

Simulation must exist in gazebo

Must be ROS2 compatible

Must have freestanding, no dependencies option

Must have no code controller tuning option

Wirelessly programmable

Must have aesthetically pleasing, easy to access web UI for logging, debugging

Safety certified

## Pendulum equations

1.5kg cart

50g mass on 80g total pendulum of 60cm length

0.48cm effective COM of pendulum

Falling moment:

$$
\begin{equation} M = m \cdot g \cdot L \cdot sin(\theta) \end{equation}
$$

$$
\begin{equation} M = 0.8 * 9.81 * 0.48 * sin(20^\circ) = 1.288Nm\end{equation}
$$

Restorative moment

$$
\begin{equation} M_a = m \cdot g \cdot L \cdot cos(\theta) \end{equation}
$$

Assuming we want to also do kick up control, we must then design a system that can easily produce a moment greater than the $M_a$.

$$
\begin{equation} 1.288= 0.8 * a * 0.48 * cos(20)\end{equation}
$$

$$
\begin{equation} a = 3.58m/s^2 \end{equation}
$$

So with a 1kg cart, we only need 3.58N of force to stabilise.

Motor force:

$$
\begin{equation} 9.81*0.22/2.5 = 0.86N \end{equation}
$$

Then with 2 motors we get 1.72N of force.

So for the system, we need a mass of less than

$$
\begin{equation} 1.72/3.58 = 0.481 \end{equation}
$$

481g in order to balance an 80g total weight pendulum of 60cm length. This is a nominal value as the stall torque is actually 3x this value, so we should be able to have a maximum producible force of 5.16N

If we take into account bursts of torque, we will be able to have up to 600g weight.

If we need, sub in similarly sized higher gear ratio motors for higher acceleration potential

Educational cart pole kit, packing 

External components:

| Component | Desc | Link | Quantity | Cost | Mass |
| --- | --- | --- | --- | --- | --- |
| JGA25-370B Motor | 620RPM motor 1:9.6 12V motor | [https://www.aliexpress.com/item/1005007793208347.html?spm=a2g0o.order_list.order_list_main.11.31141802vt2WqY#nav-specification](https://www.aliexpress.com/item/1005007793208347.html?spm=a2g0o.order_list.order_list_main.11.31141802vt2WqY#nav-specification) | x2 |  | 105gx2 |
| AS5600 Magnetic Encoder |  |  | x1 |  |  |
| 12mm wooden dowel | 60cm wooden dowel |  | x1 |  | 30g |
| Bearings |  |  | x2 |  | 7gx2 |
| 30mm wheel |  |  | x3 |  |  |
| PCB |  |  | x1 |  |  |
| Pendulum mass |  |  | x1 |  | 50g |
| Battery | 81x39x19 | [https://www.hobbyrc.co.uk/gnb-1100mah-3s-140c-lipo-battery](https://www.hobbyrc.co.uk/gnb-1100mah-3s-140c-lipo-battery) | x1 |  | 111g |

Current mass total: 501g exluding pendulum.

PCB components:

| Component | Desc | Link | Quantity | Cost |
| --- | --- | --- | --- | --- |
| STM32G4 |  |  | x1 |  |
| ESP32 C3 comms board |  |  | x1 |  |
| H bridge IC |  |  | x2 |  |
| Voltage regulator IC |  |  | x1 |  |
| Kill switch |  |  | x1 |  |

[BOM](BOM%2034de4c8a18c6803bafcff688ac84e60d.csv)

Current total components per PCB: £10.38

Capacitor characteristics 1uF 0402 at 3.3V: 42% derating gives 0.58uF, at 12V, 87% derating gives 0.13uF

[Blog post](https://www.notion.so/Blog-post-351e4c8a18c6809796a8e00ec21da670?pvs=21)
