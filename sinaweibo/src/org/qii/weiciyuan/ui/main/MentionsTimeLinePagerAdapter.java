package org.qii.weiciyuan.ui.main;

import org.qii.weiciyuan.support.lib.AppFragmentPagerAdapter;
import org.qii.weiciyuan.ui.maintimeline.CommentsByMeTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.CommentsToMeTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.MentionsCommentTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.MentionsWeiboTimeLineFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;

/**
 * User: qii
 * Date: 13-3-8
 */
public class MentionsTimeLinePagerAdapter extends AppFragmentPagerAdapter {

    private SparseArray<Fragment> fragmentList;

    public MentionsTimeLinePagerAdapter(MentionsTimeLine fragment, ViewPager viewPager, FragmentManager fm, SparseArray<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
        fragmentList.append(MentionsTimeLine.MENTIONS_WEIBO_CHILD_POSITION, fragment.getMentionsWeiboTimeLineFragment());
        fragmentList.append(MentionsTimeLine.MENTIONS_COMMENT_CHILD_POSITION, fragment.getMentionsCommentTimeLineFragment());
        fragmentList.append(MentionsTimeLine.COMMENTS_TO_ME_CHILD_POSITION, fragment.getCommentsTomeTimeLineFragment());
        fragmentList.append(MentionsTimeLine.COMMENTS_BY_ME_CHILD_POSITION, fragment.getCommentsBymeTimeLineFragment());

        FragmentTransaction transaction = fragment.getChildFragmentManager().beginTransaction();
        if (!fragmentList.get(MentionsTimeLine.MENTIONS_WEIBO_CHILD_POSITION).isAdded()){
            transaction.add(viewPager.getId(), fragmentList.get(MentionsTimeLine.MENTIONS_WEIBO_CHILD_POSITION), MentionsWeiboTimeLineFragment.class.getName());
        }
        if (!fragmentList.get(MentionsTimeLine.MENTIONS_COMMENT_CHILD_POSITION).isAdded()){
            transaction.add(viewPager.getId(), fragmentList.get(MentionsTimeLine.MENTIONS_COMMENT_CHILD_POSITION), MentionsCommentTimeLineFragment.class.getName());
        }
        if (!fragmentList.get(MentionsTimeLine.COMMENTS_TO_ME_CHILD_POSITION).isAdded()){
            transaction.add(viewPager.getId(), fragmentList.get(MentionsTimeLine.COMMENTS_TO_ME_CHILD_POSITION), CommentsToMeTimeLineFragment.class.getName());
        }
        if (!fragmentList.get(MentionsTimeLine.COMMENTS_BY_ME_CHILD_POSITION).isAdded()){
            transaction.add(viewPager.getId(), fragmentList.get(MentionsTimeLine.COMMENTS_BY_ME_CHILD_POSITION), CommentsByMeTimeLineFragment.class.getName());
        }
        if (!transaction.isEmpty()) {
            transaction.commit();
            fragment.getChildFragmentManager().executePendingTransactions();
        }
    }


    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    protected String getTag(int position) {
        SparseArray<String> tagList = new SparseArray<String>();
        tagList.append(MentionsTimeLine.MENTIONS_WEIBO_CHILD_POSITION, MentionsWeiboTimeLineFragment.class.getName());
        tagList.append(MentionsTimeLine.MENTIONS_COMMENT_CHILD_POSITION, MentionsCommentTimeLineFragment.class.getName());
        tagList.append(MentionsTimeLine.COMMENTS_TO_ME_CHILD_POSITION, CommentsToMeTimeLineFragment.class.getName());
        tagList.append(MentionsTimeLine.COMMENTS_BY_ME_CHILD_POSITION, CommentsByMeTimeLineFragment.class.getName());

        return tagList.get(position);
    }

    @Override
    public int getCount() {
        return 4;//yangxj@20140228
    }


}