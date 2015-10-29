--sql script to create avs_v2 data

-- create database avs_v2;
-- create user avs;
-- grant all privileges on database avs_v2 to avs;
-- psql -U postgres avs_v2 < initial_populate.sql

-- <app>.status [0:unreviewed, 1:accepted, 2:rejected, 3:resolved]

-- schema

set statement_timeout = 0;
set client_encoding = 'sql_ascii';
set standard_conforming_strings = on;
set check_function_bodies = false;
set client_min_messages = warning;
create schema public;
create extension if not exists plpgsql with schema pg_catalog;
comment on extension plpgsql is 'pl/pgsql procedural language';
set search_path = public, pg_catalog;
set default_tablespace = '';
set default_with_oids = false;


-- DEPARTMENTS TABLE
create table departments (
    id integer not null,
    name character varying(50),
    created timestamp without time zone
);
alter table public.departments owner to avs;
create sequence departments_id_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;
alter table public.departments_id_seq owner to avs;
alter sequence departments_id_seq owned by departments.id;
alter table only departments alter column id set default nextval('departments_id_seq'::regclass);
alter table only departments
    add constraint departments_pkey primary key (id);
-- DEPARTMENTS TABLE

-- EMPLOYEES TABLE
create table employees (
    id integer not null,
    email character varying(100),
    first_name character varying(50),
    last_name character varying(50),
    role character varying(50),
    department integer,
    created timestamp without time zone,
    current boolean
);
alter table public.employees owner to avs;
create sequence employees_id_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;
alter table public.employees_id_seq owner to avs;
alter sequence employees_id_seq owned by employees.id;
alter table only employees alter column id set default nextval('employees_id_seq'::regclass);
alter table only employees
    add constraint employees_pkey primary key (id);
alter table only employees
    add constraint employees_department_fkey foreign key (department) references departments(id);
-- EMPLOYEES TABLE

-- AVS_USERS TABLE
create table avs_users (
    "timestamp" timestamp without time zone,
    emp_id integer,
    username character varying(100),
    email character varying(100),
    active boolean
);
alter table public.avs_users owner to avs;
alter table only avs_users
    add constraint avs_users_emp_id_fkey foreign key (emp_id) references employees(id);
-- AVS_USERS TABLE

-- BT_APPLICATIONS TABLE
create table bt_applications (
    id integer not null,
    name character varying(100),
    dept_responsible integer,
    created timestamp without time zone,
    filename character varying(50),
    tablename character varying(50),
    last_import timestamp without time zone,
    active boolean,
    external_access boolean
);
alter table public.bt_applications owner to avs;
create sequence bt_applications_id_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;
alter table public.bt_applications_id_seq owner to avs;
alter sequence bt_applications_id_seq owned by bt_applications.id;
alter table only bt_applications alter column id set default nextval('bt_applications_id_seq'::regclass);
alter table only bt_applications
    add constraint bt_applications_dept_responsible_fkey foreign key (dept_responsible) references departments(id);
alter table only bt_applications
    add constraint bt_applications_pkey primary key (id);
-- BT_APPLICATIONS TABLE



-- UNIX TABLE
create table unix (
    id integer not null,
    "timestamp" timestamp without time zone,
    emp_id integer,
    app_id integer,
    username character varying(100),
    email character varying(100),
    detail character varying(500),
    actor integer,
    action_time timestamp without time zone,
    comments text,
    status integer
);
alter table public.unix owner to avs;
create sequence unix_id_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;
alter table public.unix_id_seq owner to avs;
alter sequence unix_id_seq owned by unix.id;
alter table only unix alter column id set default nextval('unix_id_seq'::regclass);
alter table only unix
    add constraint unix_pkey primary key (id);
alter table only unix
    add constraint unix_actor_fkey foreign key (actor) references employees(id);
alter table only unix
    add constraint unix_app_id_fkey foreign key (app_id) references bt_applications(id);
alter table only unix
    add constraint unix_emp_id_fkey foreign key (emp_id) references employees(id);
--- END UNIX

-- RT TABLE
create table rt (
    id integer not null,
    "timestamp" timestamp without time zone,
    emp_id integer,
    app_id integer,
    username character varying(100),
    email character varying(100),
    detail character varying(500),
    actor integer,
    action_time timestamp without time zone,
    comments text,
    status integer
);
alter table public.rt owner to avs;
create sequence rt_id_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;
alter table public.rt_id_seq owner to avs;
alter sequence rt_id_seq owned by rt.id;
alter table only rt alter column id set default nextval('rt_id_seq'::regclass);
alter table only rt
    add constraint rt_pkey primary key (id);
alter table only rt
    add constraint rt_actor_fkey foreign key (actor) references employees(id);
alter table only rt
    add constraint rt_app_id_fkey foreign key (app_id) references bt_applications(id);
alter table only rt
    add constraint rt_emp_id_fkey foreign key (emp_id) references employees(id);
--- END RT

-- BIOLOCK_USER TABLE
create table biolock_user (
    id integer not null,
    "timestamp" timestamp without time zone,
    emp_id integer,
    app_id integer,
    username character varying(100),
    email character varying(100),
    detail character varying(500),
    actor integer,
    action_time timestamp without time zone,
    comments text,
    status integer
);
alter table public.biolock_user owner to avs;
create sequence biolock_user_id_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;
alter table public.biolock_user_id_seq owner to avs;
alter sequence biolock_user_id_seq owned by biolock_user.id;
alter table only biolock_user alter column id set default nextval('biolock_user_id_seq'::regclass);
alter table only biolock_user
    add constraint biolock_user_pkey primary key (id);
alter table only biolock_user
    add constraint biolock_user_actor_fkey foreign key (actor) references employees(id);
alter table only biolock_user
    add constraint biolock_user_app_id_fkey foreign key (app_id) references bt_applications(id);
alter table only biolock_user
    add constraint biolock_user_emp_id_fkey foreign key (emp_id) references employees(id);
--- END BIOLOCK_USER

-- BIOLOCK_DEVICE TABLE
create table biolock_device (
    id integer not null,
    "timestamp" timestamp without time zone,
    emp_id integer,
    app_id integer,
    username character varying(100),
    email character varying(100),
    detail character varying(500),
    actor integer,
    action_time timestamp without time zone,
    comments text,
    status integer
);
alter table public.biolock_device owner to avs;
create sequence biolock_device_id_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;
alter table public.biolock_device_id_seq owner to avs;
alter sequence biolock_device_id_seq owned by biolock_device.id;
alter table only biolock_device alter column id set default nextval('biolock_device_id_seq'::regclass);
alter table only biolock_device
    add constraint biolock_device_pkey primary key (id);
alter table only biolock_device
    add constraint biolock_device_actor_fkey foreign key (actor) references employees(id);
alter table only biolock_device
    add constraint biolock_device_app_id_fkey foreign key (app_id) references bt_applications(id);
alter table only biolock_device
    add constraint biolock_device_emp_id_fkey foreign key (emp_id) references employees(id);
-- BIOLOCK_DEVICE TABLE

-- JDE TABLE
create table jde (
    id integer not null,
    "timestamp" timestamp without time zone,
    emp_id integer,
    app_id integer,
    username character varying(100),
    email character varying(100),
    detail character varying(500),
    actor integer,
    action_time timestamp without time zone,
    comments text,
    status integer
);
alter table public.jde owner to avs;
create sequence jde_id_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;
alter table public.jde_id_seq owner to avs;
alter sequence jde_id_seq owned by jde.id;
alter table only jde alter column id set default nextval('jde_id_seq'::regclass);
alter table only jde
    add constraint jde_pkey primary key (id);
alter table only jde
    add constraint jde_actor_fkey foreign key (actor) references employees(id);
alter table only jde
    add constraint jde_app_id_fkey foreign key (app_id) references bt_applications(id);
alter table only jde
    add constraint jde_emp_id_fkey foreign key (emp_id) references employees(id);
-- JDE TABLE

-- NETWORK TABLE
create table network (
    id integer not null,
    "timestamp" timestamp without time zone,
    emp_id integer,
    app_id integer,
    username character varying(100),
    email character varying(100),
    detail character varying(500),
    actor integer,
    action_time timestamp without time zone,
    comments text,
    status integer
);
alter table public.network owner to avs;
create sequence network_id_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;
alter table public.network_id_seq owner to avs;
alter sequence network_id_seq owned by network.id;
alter table only network alter column id set default nextval('network_id_seq'::regclass);
alter table only network
    add constraint network_pkey primary key (id);
alter table only network
    add constraint network_actor_fkey foreign key (actor) references employees(id);
alter table only network
    add constraint network_app_id_fkey foreign key (app_id) references bt_applications(id);
alter table only network
    add constraint network_emp_id_fkey foreign key (emp_id) references employees(id);
-- NETWORK TABLE	

-- AU_PORTAL TABLE
create table au_portal (
    id integer not null,
    "timestamp" timestamp without time zone,
    emp_id integer,
    app_id integer,
    username character varying(100),
    email character varying(100),
    detail character varying(500),
    actor integer,
    action_time timestamp without time zone,
    comments text,
    status integer
);
alter table public.au_portal owner to avs;
create sequence au_portal_id_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;
alter table public.au_portal_id_seq owner to avs;
alter sequence au_portal_id_seq owned by au_portal.id;
alter table only au_portal alter column id set default nextval('au_portal_id_seq'::regclass);
alter table only au_portal
    add constraint au_portal_pkey primary key (id);
alter table only au_portal
    add constraint au_portal_actor_fkey foreign key (actor) references employees(id);
alter table only au_portal
    add constraint au_portal_app_id_fkey foreign key (app_id) references bt_applications(id);
alter table only au_portal
    add constraint au_portal_emp_id_fkey foreign key (emp_id) references employees(id);
-- AU_PORAL TABLE	

-- RECREG TABLE	
create table recreg (
    id integer not null,
    "timestamp" timestamp without time zone,
    emp_id integer,
    app_id integer,
    username character varying(100),
    email character varying(100),
    detail character varying(500),
    actor integer,
    action_time timestamp without time zone,
    comments text,
    status integer
);
alter table public.recreg owner to avs;
create sequence recreg_id_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;
alter table public.recreg_id_seq owner to avs;
alter sequence recreg_id_seq owned by recreg.id;
alter table only recreg alter column id set default nextval('recreg_id_seq'::regclass);
alter table only recreg
    add constraint recreg_pkey primary key (id);
alter table only recreg
    add constraint recreg_actor_fkey foreign key (actor) references employees(id);
alter table only recreg
    add constraint recreg_app_id_fkey foreign key (app_id) references bt_applications(id);
alter table only recreg
    add constraint recreg_emp_id_fkey foreign key (emp_id) references employees(id);
-- RECREG TABLE	

-- TITAN TABLE	
create table titan (
    id integer not null,
    "timestamp" timestamp without time zone,
    emp_id integer,
    app_id integer,
    username character varying(100),
    email character varying(100),
    detail character varying(500),
    actor integer,
    action_time timestamp without time zone,
    comments text,
    status integer
);
alter table public.titan owner to avs;
create sequence titan_id_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;
alter table public.titan_id_seq owner to avs;
alter sequence titan_id_seq owned by titan.id;
alter table only titan alter column id set default nextval('titan_id_seq'::regclass);
alter table only titan
    add constraint titan_pkey primary key (id);
alter table only titan
    add constraint titan_actor_fkey foreign key (actor) references employees(id);
alter table only titan
    add constraint titan_app_id_fkey foreign key (app_id) references bt_applications(id);
alter table only titan
    add constraint titan_emp_id_fkey foreign key (emp_id) references employees(id);
-- TITAN TABLE	

-- JIRA TABLE	
create table jira (
    id integer not null,
    "timestamp" timestamp without time zone,
    emp_id integer,
    app_id integer,
    username character varying(100),
    email character varying(100),
    detail character varying(500),
    actor integer,
    action_time timestamp without time zone,
    comments text,
    status integer
);
alter table public.jira owner to avs;
create sequence jira_id_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;
alter table public.jira_id_seq owner to avs;
alter sequence jira_id_seq owned by jira.id;
alter table only jira alter column id set default nextval('jira_id_seq'::regclass);
alter table only jira
    add constraint jira_pkey primary key (id);
alter table only jira
    add constraint jira_actor_fkey foreign key (actor) references employees(id);
alter table only jira
    add constraint jira_app_id_fkey foreign key (app_id) references bt_applications(id);
alter table only jira
    add constraint jira_emp_id_fkey foreign key (emp_id) references employees(id);
-- JIRA TABLE

-- ACTIVE_DIRECTORY TABLE	
create table active_directory (
    id integer not null,
    "timestamp" timestamp without time zone,
    emp_id integer,
    app_id integer,
    username character varying(100),
    email character varying(100),
    detail character varying(500),
    actor integer,
    action_time timestamp without time zone,
    comments text,
    status integer
);
alter table public.active_directory owner to avs;
create sequence active_directory_id_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;
alter table public.active_directory_id_seq owner to avs;
alter sequence active_directory_id_seq owned by active_directory.id;
alter table only active_directory alter column id set default nextval('active_directory_id_seq'::regclass);
alter table only active_directory
    add constraint active_directory_pkey primary key (id);
alter table only active_directory
    add constraint active_directory_actor_fkey foreign key (actor) references employees(id);
alter table only active_directory
    add constraint active_directory_app_id_fkey foreign key (app_id) references bt_applications(id);
alter table only active_directory
    add constraint active_directory_emp_id_fkey foreign key (emp_id) references employees(id);
-- ACTIVE_DIRECTORY TABLE


revoke all on schema public from public;
revoke all on schema public from postgres;
grant all on schema public to postgres;
grant all on schema public to public;


-- departments
insert into departments (name, created) values ('it', now());
insert into departments (name, created) values ('dev', now());
insert into departments (name, created) values ('hr', now());
insert into departments (name, created) values ('legal', now());
insert into departments (name, created) values ('rs-r', now());

-- bt_applications
insert into bt_applications (name, filename, tablename, dept_responsible, created, last_import, active, external_access) values('Unix','unix-users-txt','unix',1, now(), '1970-01-01', true, false);
insert into bt_applications (name, filename, tablename, dept_responsible, created, last_import, active, external_access) values('Windows (AD)','NA','active_directory',1, now(), '1970-01-01', true, false);
insert into bt_applications (name, filename, tablename, dept_responsible, created, last_import, active, external_access) values('RT','rt-data-txt','rt',1, now(), '1970-01-01', true, false);
insert into bt_applications (name, filename, tablename, dept_responsible, created, last_import, active, external_access) values('Biolock','biolock-users-txt','biolock_user',1, now(), '1970-01-01', true, false);
insert into bt_applications (name, filename, tablename, dept_responsible, created, last_import, active, external_access) values('Biolock Devices','biolock-units-txt','biolock_device',1, now(), '1970-01-01', true, false);
insert into bt_applications (name, filename, tablename, dept_responsible, created, last_import, active, external_access) values('JDE','jde-data-txt','jde',1, now(), '1970-01-01', true, false);
insert into bt_applications (name, filename, tablename, dept_responsible, created, last_import, active, external_access) values('Network','network-users-txt','network',1, now(), '1970-01-01', true, false);
insert into bt_applications (name, filename, tablename, dept_responsible, created, last_import, active, external_access) values('JIRA','jira-users-txt','jira',1, now(), '1970-01-01', true, true);
insert into bt_applications (name, filename, tablename, dept_responsible, created, last_import, active, external_access) values('Portal','portal-data-txt','au_portal',1, now(), '1970-01-01', true, true);
insert into bt_applications (name, filename, tablename, dept_responsible, created, last_import, active, external_access) values('RecReg','recreg-data-txt','recreg',1, now(), '1970-01-01', true, true);
insert into bt_applications (name, filename, tablename, dept_responsible, created, last_import, active, external_access) values('Titan (Alarm)','titan-users-txt','titan',1, now(), '1970-01-01', true, true);

-- employees
insert into employees (email, first_name, last_name, role, department, created, current) values ('default@localhost', 'Default', 'user', 'employess', '1', now(), true);

--avs_users
insert into avs_users (timestamp, emp_id, username, email, active) values (now(), 1, 'mark.culhane', 'mark.culhane@bomboratech.com.au', true);

