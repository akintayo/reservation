# Getting Started

### Reference Documentation
 Online Camp Reservation API

### To Start
* From the parent of the application run docker-compose up -d 
This pulls Hazelcast and postgres database required for the application to startup

* Application runs on port **8087** 
### To Run
* To check available reservation use GET /reservation/availability to run from current date to next 30 days
    * For specific date range please provide the start and end date like this
  GET **reservation/availability?startDate=2021-07-26&endDate=2021-07-30**
  This will check for availability between July 26 and July 30
* To cancel a reservation, you need to have an existing reservationID,  use the DELETE **reservation/cancel/{{reservationID}}**

* To make a booking you can use the POST **reservation/book** 
A sample request body is
```  
    "fullName": "James Eyre",
    "checkInDate": "2021-07-26",
    "checkoutDate": "2021-07-27",
    "email" : "john.doe@go.com"
```

* To modify a booking you can use the PUT **reservation/modify**
  A sample request body is
```  
    "bookingReferenceId": "daac6cbc-0323-4701-8222-395fa8bef9b3",
    "checkInDate": "2021-07-26",
    "checkoutDate": "2021-07-27"    
```