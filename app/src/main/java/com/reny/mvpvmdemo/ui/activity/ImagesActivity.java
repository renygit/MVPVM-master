package com.reny.mvpvmdemo.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.markzhai.recyclerview.BaseViewAdapter;
import com.github.markzhai.recyclerview.BindingViewHolder;
import com.github.markzhai.recyclerview.SingleTypeAdapter;
import com.reny.mvpvmdemo.MyApplication;
import com.reny.mvpvmdemo.R;
import com.reny.mvpvmdemo.core.MyBaseActivity;
import com.reny.mvpvmdemo.core.MyBaseViewModel;
import com.reny.mvpvmdemo.core.SingleAdapter;
import com.reny.mvpvmdemo.databinding.ActivityImagesBinding;
import com.reny.mvpvmdemo.entity.other.ImgsInfo;
import com.reny.mvpvmdemo.utils.Constans;
import com.reny.mvpvmdemo.utils.DateTimeUtils;
import com.reny.mvpvmdemo.utils.FileUtils;
import com.reny.mvpvmdemo.utils.SwipeBackUtils;
import com.reny.mvpvmdemo.utils.ToastUtils;
import com.reny.mvpvmdemo.utils.img.ImageUtils;
import com.reny.mvpvmdemo.utils.img.glide.DownCallBack;
import com.reny.mvpvmdemo.widget.DividerItemDecoration;
import com.reny.mvpvmlib.base.RBasePresenter;
import com.reny.mvpvmlib.base.RBaseViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.observers.DisposableObserver;

public class ImagesActivity extends MyBaseActivity<ActivityImagesBinding> {

    private DraweePagerAdapter draweePagerAdapter;
    public List<String> imgsList;
    private RBasePresenter presenter;

    @Override
    protected void init(Bundle savedInstanceState) {
        SwipeBackUtils.DisableSwipeActivity(this);
        binding.flRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if(null != getIntent()){
            ImgsInfo imgsInfo = (ImgsInfo) getIntent().getSerializableExtra(ImgsInfo.KEY);
            if(null == imgsInfo)return;

            binding.vpImg.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));

            draweePagerAdapter = new DraweePagerAdapter();
            imgsList = imgsInfo.getImgsList();
            draweePagerAdapter.setImgsUrl(imgsList);

            binding.vpImg.setAdapter(draweePagerAdapter);
            binding.vpImg.setCurrentItem(imgsInfo.getCurPos());
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_images;
    }

    @Override
    protected RBasePresenter getPresenter() {
        if(null == presenter){
            presenter = new RBasePresenter(this, new RBaseViewModel()) {
                @Override
                public void onCreate() {}
            };
        }
        return presenter;
    }

    public class DraweePagerAdapter extends PagerAdapter implements BaseViewAdapter.Presenter{

        private List<String> imgsUrl;
        private String imgUrl;

        private RecyclerView rv;
        private SingleAdapter<String> adapter = new SingleAdapter<>(MyApplication.getContext(), R.layout.view_simple_sheet);
        private BottomSheetDialog sheetDialog;
        private List<String> sheetNames;

        private void setImgsUrl(List<String> imgsUrl){
            this.imgsUrl = imgsUrl;
        }

        @Override
        public int getCount() {
            return imgsUrl.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup viewGroup, final int position) {
            PhotoView photoView = new PhotoView(viewGroup.getContext());
            ImageUtils.getInstance().disPlay(imgsUrl.get(position), photoView);
            photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
                @Override
                public void onPhotoTap(ImageView view, float x, float y) {
                    finish();
                }
            });
            photoView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //ToastUtils.showShort("pos:"+position);
                    imgUrl = imgsUrl.get(position);
                    longPressImg(v);
                    return false;
                }
            });
            try {
                viewGroup.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            } catch (Exception e) {e.printStackTrace();}
            return photoView;
        }

        private void longPressImg(View v){
            Context context = v.getContext();
            if(null == rv){
                rv = new RecyclerView(context);
                rv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                rv.setLayoutManager(new MyLinearLayoutManager(context));
                rv.addItemDecoration(DividerItemDecoration.get1pxDividerV(context));
                rv.setAdapter(adapter);
            }
            if(null == sheetNames) sheetNames = new ArrayList<>();
            else sheetNames.clear();

            sheetNames.add("保存图片");
            sheetNames.add("取消");
            adapter.set(sheetNames);

            if (null == sheetDialog) {
                sheetDialog = new BottomSheetDialog(context);
                sheetDialog.setContentView(rv);
            }
            sheetDialog.show();

            adapter.setPresenter(this);
        }

        public void onClickItem(BindingViewHolder holder){
            if (sheetDialog.isShowing()) sheetDialog.dismiss();
            if (holder.getAdapterPosition() == 0) {
                //简单处理权限问题
                presenter.checkPermissions(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        if(value){
                            String dir = FileUtils.getDownLoadImgPath();
                            if (dir != null) {
                                String imgName = "img_" + DateTimeUtils.getCurDateStr(DateTimeUtils.getFormatYMDHMS());
                                ImageUtils.getInstance().downLoadPic(getActivity(), imgUrl, dir, imgName, new DownCallBack() {
                                    @Override
                                    public void onSuccess(final File file) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ToastUtils.showLong("图片已保存到："+file.getAbsolutePath());
                                                FileUtils.notifyFileSystemChanged(file.getAbsolutePath());
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailed(final String err) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ToastUtils.showShort(err);
                                            }
                                        });
                                    }
                                });
                            } else {
                                ToastUtils.showShort(getResources().getString(R.string.no_storage));
                            }
                        }else {
                            ToastUtils.showLong(getResources().getString(R.string.no_permission_WRITE_EXTERNAL));
                        }
                    }
                    @Override
                    public void onError(Throwable e) {}
                    @Override
                    public void onComplete() {}
                }, Constans.StoragePermissions);
            }
        }

    }

    private class MyLinearLayoutManager extends LinearLayoutManager {
        private MyLinearLayoutManager(Context context) {
            super(context);
        }
        @Override
        public boolean canScrollVertically() {
            return false;
        }
    }
}