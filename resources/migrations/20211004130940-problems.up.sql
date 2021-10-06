create table problems (
  id serial primary key,
  num int, -- 本来 unique だが、順番替えしたくなる時もある。
  is_avail BOOLEAN default true,
  problem text not null,
  test text,
  create_at timestamp default current_timestamp,
  update_at timestamp default current_timestamp);
