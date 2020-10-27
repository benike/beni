create table amusement_park (
       id  bigserial not null,
        capital int4 not null check (capital<=50000 AND capital>=500),
        entrance_fee int4 not null check (entrance_fee>=5 AND entrance_fee<=200),
        name varchar(20) not null,
        total_area int4 not null check (total_area>=50 AND total_area<=5000),
        primary key (id)
    ); 
    
create table amusement_park_know_visitor (
       date_of_first_enter timestamp,
        amusement_park_id int8 not null,
        visitor_email varchar(255) not null,
        primary key (amusement_park_id, visitor_email)
    );
    
create table guest_book_registry (
       id  bigserial not null,
        date_of_registry timestamp,
        text_of_registry varchar(100) not null,
        amusement_park_id int8 not null,
        visitor_email varchar(255) not null,
        primary key (id)
    ); 
    
create table machine (
       id  bigserial not null,
        fantasy_name varchar(25) not null,
        minimum_required_age int4 not null check (minimum_required_age<=21 AND minimum_required_age>=0),
        number_of_seats int4 not null check (number_of_seats<=250 AND number_of_seats>=5),
        price int4 not null check (price<=2000 AND price>=50),
        size_of_machine int4 not null check (size_of_machine>=20 AND size_of_machine<=750),
        ticket_price int4 not null check (ticket_price>=5 AND ticket_price<=30),
        type varchar(255) not null,
        amusement_park_id int8 not null,
        primary key (id)
    ); 
    
create table photo (
       id  bigserial not null,
        photo text,
        primary key (id)
    );

create table visitor (
       email varchar(255) not null,
        authority varchar(25) not null,
        date_of_birth date not null,
        date_of_sign_up timestamp,
        password varchar(60) not null,
        spending_money int4 not null check (spending_money>=0 AND spending_money<=2147483647),
        amusement_park_id int8,
        machine_id int8,
        photo_id int8,
        primary key (email)
    );

create table visitor_event (
       id bigint generated by default as identity,
        creation_date_time timestamp,
        type varchar(255) not null,
        amusement_park_id bigint not null,
        machine_id bigint,
        visitor_email varchar(255) not null,
        primary key (id)
    );
    
alter table amusement_park_know_visitor 
       add constraint FKky6jelw83grnwfe2dkgyxpsqa 
       foreign key (amusement_park_id) 
       references amusement_park;
    
alter table amusement_park_know_visitor 
       add constraint FKeyn9ibu81k0g0gxmhnawylxo 
       foreign key (visitor_email) 
       references visitor;
    
alter table guest_book_registry 
       add constraint FKsx0gf40kmm8f7h17sf2ru43ev 
       foreign key (amusement_park_id) 
       references amusement_park;
    
alter table guest_book_registry 
       add constraint FK2f827goevhyjnveyf2walfuig 
       foreign key (visitor_email) 
       references visitor;
    
alter table machine 
       add constraint FK4sw7jc6c5kqbeee41r6g5v3wp 
       foreign key (amusement_park_id) 
       references amusement_park;
    
alter table visitor 
       add constraint FK5o2p464q48ixnxf38igu59km0 
       foreign key (amusement_park_id) 
       references amusement_park;
    
alter table visitor 
       add constraint FKpvxnx2xymk3l883u40s0n2gng 
       foreign key (machine_id) 
       references machine;

alter table visitor 
       add constraint FKm7oekhmmpefh1u3qmqtxhl693 
       foreign key (photo_id) 
       references photo;

alter table visitor_event 
       add constraint FK1y7xcpfp55c1b7b2eysk624u3 
       foreign key (amusement_park_id) 
       references amusement_park;
    
alter table visitor_event 
       add constraint FK2rese3j3ryq57gr6cxnyvbym6 
       foreign key (machine_id) 
       references machine;
    
alter table visitor_event 
       add constraint FK4uly1me9ymbhjp7jyho3u2xb3 
       foreign key (visitor_email) 
       references visitor;


CREATE TABLE SPRING_SESSION (
	PRIMARY_ID CHAR(36) NOT NULL,
	SESSION_ID CHAR(36) NOT NULL,
	CREATION_TIME BIGINT NOT NULL,
	LAST_ACCESS_TIME BIGINT NOT NULL,
	MAX_INACTIVE_INTERVAL INT NOT NULL,
	EXPIRY_TIME BIGINT NOT NULL,
	PRINCIPAL_NAME VARCHAR(100),
	CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
	SESSION_PRIMARY_ID CHAR(36) NOT NULL,
	ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
	ATTRIBUTE_BYTES BYTEA NOT NULL,
	CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
	CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);