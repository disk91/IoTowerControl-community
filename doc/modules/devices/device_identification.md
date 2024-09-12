## Device identification

A device can support different kind of identifications based on the context. The following identifications can be used:
- **Device Hw Unique Id**: The device hardware unique id is a unique identifier for the device 
in the platform ; it matches one-to-one with a hardware device.
- **Device Data Unique Id**: The Data unique id is an identifier for the data provided by a device into the platform.
The purpose is to manage a single dataset when a hardware device is swapped with another (replacement case). This allows
to maintain a continuity of the data all along the device life-cycle. When the device is re-affected to another customer,
the Data Unique Id is preserved for letting the user access it's previous data but detached from the device. This is the ID 
manipulated by the user.
- **Communication IDs**: This is an array of set of communication IDS. A single device can have different communication IDS as 
it can support multiple communications technologies and / or operators. These credential are affected at different stages. 
This information is used to route the information from the capture interfaces to the right device data.
