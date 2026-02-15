# g5-project
Final Project of PAT by group 5 


Endpoints created

POST /pistaPadel/courts
GET /pistaPadel/courts
GET /pistaPadel/courts/{courtId}
PATCH /pistaPadel/courts/{courtId}
DELETE /pistaPadel/courts/{courtId}

I created a record named Pista and added endpoints to the REST controller. In the class ConfigSeguridad I created two possible user authentications: USER and ADMIN which have different authorities to change details in the different courts. 
