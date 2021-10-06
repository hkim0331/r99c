create table problems (
  id serial primary key,
  num integer,
  is_avail BOOLEAN default true,
  problem text not null,
  test text default '',
  create_at timestamp default current_timestamp,
  update_at timestamp default current_timestamp);
