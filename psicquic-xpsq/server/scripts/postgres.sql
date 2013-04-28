create table record (
  pk bigserial,
  rid varchar(256) not null default '',
  format varchar(32) not null default '', 
  record text not null default '' );

create index r_rid on record (rid);
create index r_ft on record (format);
