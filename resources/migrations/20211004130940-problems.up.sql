create table problems (
  id serial primary key,
  num int,
  is_avail BOOLEAN default false,
  problem text not null,
  test text,
  create_at timestamp default current_timestamp,
  update_at timestamp default current_timestamp);
