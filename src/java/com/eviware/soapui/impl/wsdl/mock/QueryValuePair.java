package com.eviware.soapui.impl.wsdl.mock;

class QueryValuePair
{
   private String query;
   private String value;

   public QueryValuePair(String query, String value)
   {
      assert (query != null);
      assert (value != null);

      this.query = query;
      this.value = value;
   }

   public String getQuery()
   {
      return query;
   }

   public String getValue()
   {
      return value;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj != null && obj.getClass() == this.getClass())
      {
         if (obj == this)
         {
            return true;
         }

         QueryValuePair that = (QueryValuePair) obj;

         return (query.equals(that.query) && value.equals(that.value));
      }
      else
      {
         return false;
      }
   }

   @Override
   public int hashCode()
   {
      int hash = 7;
      hash = 31 * hash + query.hashCode();
      hash = 31 * hash + value.hashCode();

      return hash;
   }

   @Override
   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append(query).append(':').append(value);

      return sb.toString();
   }
}
