# Vi Scrapbook

[Todo](Todo%20336e4c8a18c680f3992cf78e4323d3d5.csv)

Scrapbook that plays sound/songs when a photo on the page is pressed. Paper pages, paper photos.

Requirements

| Requirement | Solutions |  |
| --- | --- | --- |
| Plays audio | Speaker + audio module |  |
| Responds to touch | Capacitive touch sensors |  |
| Lasts for 1 day+ | Power delivery system |  |
| Safe for use | Power regulator |  |
| Pages turn easily | Flexible electrode design |  |
| Indistinguishable from book | Hand bound book |  |

## Subsystems

Once requirements are found, we must break down the device into the subsystems:

Power delivery

Audio

Touch sensing

Control

Physical design

Page layout

The hardest part of this project comes under the electrical design, but all of the parts affect each other. Page layout choices constrain the electrodes, physical dimensions affect the size of the circuit board and PCB component placement.

Before starting I had a rough idea of what I needed, but deeper dives showed that I needed more. The first part was book binding techniques. I needed to integrate a circuit somewhere in the book. The book needs to be indistinguishable from a regular book if possible. The options would either be to fit the circuit in the covers, or in the spine. The covers provide more area, but when the pages turn there is more movement between the connections to electrodes in the page. Also, depending on the circuit board components, this part has to be a lot flatter. A spine based design has to be much narrower, but it has enough length to fit all of the components. It also bends, and doesn’t have to be fully connected to the back pages. A vertical stack of triple A batteries could be fit in the spine without much alteration to the book’s aesthetic. A hybrid design could be possible, but for ease of connections I decided to stick with a spine based circuit.

## Book binding

I researched book binding techniques and kits for about a week before embarking on the project. I found the following:

- Perfect binding
- Coptic Stitch
- Dutch Style
- Japanese Style
- Punch and ring bound

For a perfect bind, the issue is that the pages are not exposed at the back. There is no wiggle room for components and this is typically a paperback technique.

Coptic stitch, Japanese Style and Dutch Style all have similarities and are great for this task. The spine is exposed, and paper, folded into volumes are sewn together, with loops that sew into the hard covers. This means that we can add a non-structural spine with whatever modifications we want without affecting the binding quality. This is the method I have opted to go for.

Punch and ring binding is the easiest for this kind of book, however, in my opinion it is too mass-produced looking for a personalised gift. Also the ring constrains the dimensions of the circuit too much.

## Circuit choices

While the main purpose of the circuit is to house all of the electronic components and drive the electrical functions of the device, one of the biggest design challenges is making the board physically unobtrusive and discrete. I chose to integrate the board into the spine, so giving a rectangular shape being tall and as narrow as possible. In my iterations however I couldn’t get much smaller than 20mm, and although 2cm seems like a very narrow board, as a book spine it is quite large. Maybe in future iterations as my PCB design skills improve I will narrow this down further.

Next is the type of board. The options are flex-rigid and rigid boards. Rigid boards are made of a solid substrate, such as fibreglass (FR4) and are much thicker than their flexible counterparts. They can hold many more layers however and are easier to design for. Flexible printed circuits, although they are thinner and allow for bend rather than breaking, require more thought to the trace routing and adding stiffeners to soldered components so connections don’t break. The lead time for flexible printed circuits is also significantly longer, with a higher cost, leading to the choice of a rigid circuit board for this project.

## Book binding prototypes

With a choice of Coptic stitch binding, I set out to learn this technique well enough to bind a final product within 1 month of starting this project, on top of studying and work commitments. The tools typically used are:

- Awl
- Curved needle
- Waxed linen thread
- Bone page folder

And for materials, high quality paper with paperboard covers are used. I won’t go over the cover printing process just yet.

I started off as cheaply as possible, opting to use a £10 Argos sewing kit to try and get the job done. I bought 10 sheets of A4 paperboard and an A4 sketchpad from TG Jones, using an A3 sized book as my reference.

Using standard thread, and the thickest needle I could find showed a few issues:

- Binding outer volumes to the covers was hard, as the straight needle pushed the pages away, creating stitches that were much too loose
- The thread broke

[https://www.notion.so](https://www.notion.so)

![1000034441.jpg](1000034441.jpg)

![1000034442.jpg](1000034442.jpg)

## Page design

## Power delivery

Power delivery is a two pronged problem. I need a power source that can provide enough current and voltage while being as small and unobtrusive as possible. The requirements are:

- 4-4.5V output
- 500mA current spike (max current)

The options were as follows

| Type | Voltage | Max Current |
| --- | --- | --- |
| CR20xx | 3V | 30mA |
| Triple A | 1.5V | 1-2A |
| Quadruple A | 1.5V | 200mA |
| LiPo/LiFePo | 3.7V | Variable |
|  |  |  |

Coin batteries provide too little current to be used for audio. Triple A batteries are the best, but they have an 10.5mm diameter. Quadruple A batteries are slimmer at 8.3mm diameter, but that is still quite thick in comparison to a book spine. They don’t output enough current alone. LiPo is ideal, it has the best current characteristics and size profile, but it also carries fire/explosion risk.

Ultimately I chose to go with 3 AAAA batteries with a large bulk discharge capacitor for the speaker’s high current spikes. This way we can satisfy the current needs, while keeping a low enough profile and being safe for consumer usage.

For testing however, I soldered my circuits to a 5V USB-C power delivery breakout board.

## Electronics Mounting

The initial plan was to have all of the components: breadboard, speaker, dfplayer mini and battery all hidden in the spine. While this might have been feasible for a LiPo battery, with the AAAA batteries we would have to adapt slightly.

Here is spine V1

![image.png](image.png)

![image.png](image%201.png)

While this might look quite sleek at only 20mm wide and 210mm tall (A5 height by about 15x2 pages), it is very thick at over 10mm, mostly due to the batteries. 

V2 will integrate all components directly mounted onto a slightly thicker custom PCB, with custom flat, flexible connectors that can slot in through one gap in the pages.

## Final design

The final design comes in three sizes:

An A4 sized with 4 touch sensor arrays - most expensive

An A5 sized with 3 touch sensor arrays - most pages

An A5 sized with 2 touch sensor arrays - cheapest

A5 dimensions are 158mm x 220mm x 20mm

Status: pre-ordered the wurth flexible connectors, need to wait before ordering rest of PCB

- [ ]  FFC Design
- [ ]  FFC connector footprint
- [ ]  Battery housing design
- [ ]  Order final prototype
- [ ]  Finish book

## Prototype BOM

- [ ]  https://www.aliexpress.com/item/1005007041787476.html?spm=a2g0o.productlist.main.1.3b4465414JJVvK&algo_pvid=91fbee62-a586-413f-bae4-cb03bdcf07ca&algo_exp_id=91fbee62-a586-413f-bae4-cb03bdcf07ca-0&pdp_ext_f=%7B%22order%22%3A%22487%22%2C%22eval%22%3A%221%22%2C%22fromPage%22%3A%22search%22%7D&pdp_npi=6%40dis%21GBP%214.63%211.23%21%21%216.12%211.63%21%402103847817713381755305186e8284%2112000039193594061%21sea%21UK%210%21ABX%211%210%21n_tag%3A-29910%3Bd%3A27ce76bc%3Bm03_new_user%3A-29895%3BpisId%3A5000000197842826&curPageLogUid=0hHIYVM5RMuW&utparam-url=scene%3Asearch%7Cquery_from%3A%7Cx_object_id%3A1005007041787476%7C_p_origin_prod%3A MPR121 x 5 £4.63
- [ ]  https://www.aliexpress.com/item/1005010580012002.html?spm=a2g0o.cart.0.0.511638dagwEqa3&mp=1&pdp_npi=6%40dis%21GBP%21GBP%2019.42%21GBP%2018.25%21%21GBP%2018.25%21%21%21%402103864c17713394386801181e7f4e%2112000052880779512%21ct%21UK%216258455461%21%211%210%21 ESP32 S3 super mini x5/5 £18.25/5 = £3.65
- [ ]  Kitchen foil
- [ ]  A4 paper

Steps

- [x]  MPR121 with foil electrodes through paper
- [x]  Battery calculations for battery capacity, size, type etc
- [x]  Circuit calculations for wire length/sensitivity
- [x]  Speaker selection
- [x]  Multiple MPR121 test
- [x]  Flexible spine circuit design
- [x]  Enclosure design
- [x]  Photo selection - determines page number and layout
- [x]  Photo editing
- [x]  Book binding prototype
- [x]  Audio player selection
- [ ]  Power switch design
- [ ]  FFC design
- [ ]  Cover design
- [ ]  Song selection
- [ ]  Final PCB
- [ ]  Fill out all BOMs

[Visual page layout](https://www.notion.so/Visual-page-layout-30ae4c8a18c6806eab5de9eaa351397f?pvs=21)

[PCB + circuit layout](https://www.notion.so/PCB-circuit-layout-30ae4c8a18c6802ca01ed77bd24c4052?pvs=21)

[CAD](https://www.notion.so/CAD-30ae4c8a18c680519862d2da1eb6981a?pvs=21)

[https://www.digikey.co.uk/en/htmldatasheets/production/1654880/0/0/1/mpr121](https://www.digikey.co.uk/en/htmldatasheets/production/1654880/0/0/1/mpr121)

MPR121 x 4

STM32L0 low power Arm Cortex M0+ for low power draw and efficiency

[DFPlayer Mini Mp3 Player - DFRobot Wiki](https://wiki.dfrobot.com/dfplayer_mini_sku_dfr0299)

https://www.adafruit.com/product/4227?srsltid=AfmBOoob__iOSs6JIZXso2zGc4_KcCGMbqQKpvgGIZ0tT7k3oZq2G0xz

[Project Evaluation](https://www.notion.so/Project-Evaluation-336e4c8a18c68051a162e0227ddc1c15?pvs=21)