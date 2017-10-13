package pt.fcup;

/**
* Interface used by
- the main portal and client
- the main portal and client manager 
to communicate
*/

public interface IClient
{
  public void list_seeders();
  public void create_seed();
}
