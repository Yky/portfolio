package name.abuchen.portfolio.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public final class Security
{
    public static final class ByName implements Comparator<Security>, Serializable
    {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(Security s1, Security s2)
        {
            if (s1 == null)
                return s2 == null ? 0 : -1;
            return s1.name.compareTo(s2.name);
        }
    }

    public enum AssetClass
    {
        CASH, DEBT, EQUITY, REAL_ESTATE, COMMODITY;
    }

    private String name;

    private String isin;
    private String tickerSymbol;

    private AssetClass type;
    private String industryClassification;

    private String feed;
    private List<SecurityPrice> prices = new ArrayList<SecurityPrice>();
    private LatestSecurityPrice latest;

    public Security()
    {}

    public Security(String name, String isin, String tickerSymbol, AssetClass type, String feed)
    {
        this.name = name;
        this.isin = isin;
        this.tickerSymbol = tickerSymbol;
        this.type = type;
        this.feed = feed;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getIsin()
    {
        return isin;
    }

    public void setIsin(String isin)
    {
        this.isin = isin;
    }

    public String getTickerSymbol()
    {
        return tickerSymbol;
    }

    public void setTickerSymbol(String tickerSymbol)
    {
        this.tickerSymbol = tickerSymbol;
    }

    public String getIndustryClassification()
    {
        return industryClassification;
    }

    public void setIndustryClassification(String industryClassification)
    {
        this.industryClassification = industryClassification;
    }

    public AssetClass getType()
    {
        return type;
    }

    public void setType(AssetClass type)
    {
        this.type = type;
    }

    public String getFeed()
    {
        return feed;
    }

    public void setFeed(String feed)
    {
        this.feed = feed;
    }

    public List<SecurityPrice> getPrices()
    {
        return Collections.unmodifiableList(prices);
    }

    public void addPrice(SecurityPrice price)
    {
        int index = Collections.binarySearch(prices, price);

        if (index < 0)
        {
            prices.add(price);
            Collections.sort(prices);
        }
        else
        {
            prices.set(index, price);
        }
    }

    public void removePrice(SecurityPrice price)
    {
        prices.remove(price);
    }

    public void removeAllPrices()
    {
        prices.clear();
    }

    public SecurityPrice getSecurityPrice(Date time)
    {
        if (prices.isEmpty())
        {
            if (latest != null)
                return latest;
            else
                return new SecurityPrice(time, 0);
        }

        // prefer latest quotes
        if (latest != null)
        {
            SecurityPrice last = prices.get(prices.size() - 1);

            // if 'last' younger than 'requested'
            if (last.getTime().getTime() < time.getTime())
            {
                // if 'latest' older than 'last' -> 'latest' (else 'last')
                if (latest.getTime().getTime() >= last.getTime().getTime())
                    return latest;
                else
                    return last;
            }
        }

        if (prices.isEmpty())
            return null;

        SecurityPrice p = new SecurityPrice(time, 0);
        int index = Collections.binarySearch(prices, p);

        if (index >= 0)
            return prices.get(index);
        else
            return prices.get(Math.max(-index - 2, 0));
    }

    public LatestSecurityPrice getLatest()
    {
        return latest;
    }

    public void setLatest(LatestSecurityPrice latest)
    {
        this.latest = latest;
    }

    public List<Transaction> getTransactions(Client client)
    {
        List<Transaction> answer = new ArrayList<Transaction>();

        for (Account account : client.getAccounts())
        {
            for (AccountTransaction t : account.getTransactions())
            {
                if (t.getSecurity() == null || !t.getSecurity().equals(this))
                    continue;

                switch (t.getType())
                {
                    case INTEREST:
                    case DIVIDENDS:
                        answer.add(t);
                        break;
                    case FEES:
                    case TAXES:
                    case DEPOSIT:
                    case REMOVAL:
                    case BUY:
                    case SELL:
                    case TRANSFER_IN:
                    case TRANSFER_OUT:
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }

        for (Portfolio portfolio : client.getPortfolios())
        {
            for (PortfolioTransaction t : portfolio.getTransactions())
            {
                if (!t.getSecurity().equals(this))
                    continue;

                switch (t.getType())
                {
                    case TRANSFER_IN:
                    case TRANSFER_OUT:
                    case BUY:
                    case SELL:
                        answer.add(t);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }

        return answer;
    }

    public Security deepCopy()
    {
        Security answer = new Security();

        answer.name = name;
        answer.isin = isin;
        answer.tickerSymbol = tickerSymbol;
        answer.type = type;
        answer.industryClassification = industryClassification;

        answer.feed = feed;
        answer.prices = new ArrayList<SecurityPrice>(prices);
        answer.latest = latest;

        return answer;
    }

    @Override
    public String toString()
    {
        return getName();
    }

}