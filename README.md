## IoTowerControl

This project is an open-source IoT platform made to support industrial and scalable project.
The current status is "in progress" and the project is not yet ready for being used.
It's currently published for sharing the documentation aspects, concepts and the architecture.

### Installation

```bash
# eventualy setup the data directory root by editing Makefile __CONF_DIR__ variable  
# deploy the project data tree 
$ make install

# edit the $(CONF_DIR)/docker_compose.yml file to set the project environment variables
# then if you want to use nginx as a container / setup domain name in DOMAIN_NAME and SECONDARY_NAME when multiple
$ make setup_nginx

# The system is ready to start
$ make start
```


## Non Community Edition

The non-Community version will provide additional features, as described below.
- `Support ticket management` with an AI integration that makes user interaction easier and enables automated responses. 
   This also includes the management of public contact forms, as well as both public and private FAQs.
- `Contact` It supports the management of external contacts, making it possible to extend alerting and automation 
   scenarios to users who are not registered on the platform.


### Additional Features
 
- Captcha support on registration
