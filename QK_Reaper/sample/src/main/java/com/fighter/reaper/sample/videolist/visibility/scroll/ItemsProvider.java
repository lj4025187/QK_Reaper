package com.fighter.reaper.sample.videolist.visibility.scroll;


import com.fighter.reaper.sample.videolist.visibility.items.ListItem;

/**
 * This interface is used by {@link com.fighter.sample.videolist.visibility.calculator.SingleListViewItemActiveCalculator}.
 * Using this class to get {@link com.fighter.sample.videolist.visibility.items.ListItem}
 *
 * @author Wayne
 */
public interface ItemsProvider {

    ListItem getListItem(int position);

    int listItemSize();

}
